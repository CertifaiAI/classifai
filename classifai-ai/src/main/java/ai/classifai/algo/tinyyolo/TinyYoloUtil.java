package ai.classifai.algo.tinyyolo;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.opencv.opencv_core.Size;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.transform.ColorConversionTransform;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.zoo.ZooModel;
import org.deeplearning4j.zoo.model.TinyYOLO;
import org.deeplearning4j.zoo.util.darknet.VOCLabels;
import java.io.IOException;

import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2RGB;

@Slf4j
public class TinyYoloUtil
{
    @Getter private ComputationGraph baseModel;
    @Getter private VOCLabels labels;
    @Getter private TinyYOLO zooModel;

    private final int IMAGE_WIDTH = 416;
    private final int  IMAGE_HEIGHT = 416;

    @Getter private Size defaultSize = null;
    @Getter private NativeImageLoader loader = null;

    private final int GRID_WIDTH = 13;
    private final int GRID_HEIGHT = 13;

    private final double DETECTION_THRESHOLD = 0.5;
    private final double NMS_THRESHOLD = 0.4;

    private void resetLoader()
    {
        baseModel = null;
        zooModel = null;
        labels = null;
        defaultSize = null;
    }

    public Size getImageSize()
    {
        return defaultSize;
    }

    public NativeImageLoader getLoader()
    {
        return loader;
    }

    public double getDetThres()
    {
        return DETECTION_THRESHOLD;
    }

    public double getNMSThres()
    {
        return NMS_THRESHOLD;
    }

    public int getGridWidth()
    {
        return GRID_WIDTH;
    }

    public int getGridHeight()
    {
        return GRID_HEIGHT;
    }


    public TinyYoloUtil()
    {
        ZooModel model = TinyYOLO.builder().numClasses(0).build();

        zooModel = (TinyYOLO) model;

        try
        {
            baseModel= (ComputationGraph) model.initPretrained();
        }
        catch(IOException e)
        {
            resetLoader();
            log.error("Error in loading TinyYolo model, ", e);
            return;
        }

        try
        {
            labels = new VOCLabels();
        }
        catch(IOException e)
        {
            resetLoader();
            log.error("Error in loading labels for TinyYolo, ", e);
        }

        defaultSize = new Size(IMAGE_WIDTH, IMAGE_HEIGHT);
        loader = new NativeImageLoader(IMAGE_WIDTH, IMAGE_HEIGHT, 3, new ColorConversionTransform(COLOR_BGR2RGB));

    }
}
