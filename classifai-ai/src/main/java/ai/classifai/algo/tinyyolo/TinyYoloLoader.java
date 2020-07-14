package ai.classifai.algo.tinyyolo;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;

import org.deeplearning4j.nn.layers.objdetect.DetectedObject;
import org.deeplearning4j.nn.layers.objdetect.YoloUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.List;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

@Slf4j
public class TinyYoloLoader
{
    TinyYoloUtil util;

    public TinyYoloLoader()
    {
        util = new TinyYoloUtil();
    }

    public void getInference(String imagePath)
    {
        Mat inputImage = imread(imagePath);

        int imageHeight = inputImage.rows();
        int imageWidth = inputImage.cols();

        INDArray imageArray = preprocessImage(inputImage);

        INDArray outputs = util.getBaseModel().outputSingle(imageArray);

        INDArray boundingBoxPriors = Nd4j.create(util.getZooModel().getPriorBoxes());

        List<DetectedObject> objs = YoloUtils.getPredictedObjects(boundingBoxPriors, outputs, util.getDetThres(), util.getNMSThres());

        for (DetectedObject obj : objs)
        {
            double[] xy1 = obj.getTopLeftXY();
            double[] xy2 = obj.getBottomRightXY();
            String label = util.getLabels().getLabel(obj.getPredictedClass());

            int x1 = (int) Math.round(imageWidth * xy1[0] / util.getGridWidth());
            int y1 = (int) Math.round(imageHeight * xy1[1] / util.getGridHeight());
            int x2 = (int) Math.round(imageWidth * xy2[0] / util.getGridWidth());
            int y2 = (int) Math.round(imageHeight * xy2[1] / util.getGridHeight());

            rectangle(inputImage, new Point(x1, y1), new Point(x2, y2), Scalar.RED, 2, 0, 0);
            putText(inputImage, label, new Point(x1 + 2, y2 - 2), FONT_HERSHEY_DUPLEX, 1, Scalar.GREEN);
        }
        imwrite("C:\\Users\\chiaw\\Desktop\\sample.jpg", inputImage);

        System.out.println("Image saved");
    }

    private INDArray preprocessImage(Mat inputImage)
    {
        Mat resizedImage = new Mat();

        resize(inputImage, resizedImage, util.getImageSize());

        INDArray array;

        try
        {
            array = util.getLoader().asMatrix(resizedImage);
        }
        catch(Exception e)
        {
            log.debug("Image loading failed: ", e);
            return null;
        }

        return array;
    }
}
