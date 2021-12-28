package ai.classifai.util.data;

import ai.classifai.database.annotation.AnnotationVerticle;
import ai.classifai.database.portfolio.PortfolioVerticle;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.selector.status.FileSystemStatus;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class VideoHandler {

    private static final boolean saveFramesToFolder = true;
    @Getter private static int timeStamp = 0;
    @Getter private static final int batchSize = 50;
    @Setter private static int frameNumberStartPoint = 0;
    @Getter @Setter private static int videoLength = 0;
    @Getter private static int numOfGeneratedFrames = 0;
    @Getter private static Map<Integer, List<String>> frameExtractionMap = new LinkedHashMap<>();
    @Getter private static final List<String> base64Frames = new ArrayList<>();

    public static void extractFrames(String videoPath, Integer extractionPartition, String projectPath) {

        VideoCapture cap = new VideoCapture();
        cap.open(videoPath);
        int extractionInterval = frameNumberStartPoint + batchSize;
        cap.set(Videoio.CAP_PROP_POS_FRAMES, frameNumberStartPoint);

        frameExtractionMap.clear();
        if(cap.isOpened())
        {
            Mat frame = new Mat();

            int generatedFrames = numOfGeneratedFrames;

            //for partition use
            int frameIndex = 0;

            log.info("Extracting frames from video....");
            log.info("Saving output to " + projectPath);

            while(cap.read(frame)) //the last frame of the movie will be invalid. check for it !
            {
                if (generatedFrames == extractionInterval) {
                    break;
                }

                base64Frames.add(base64FromBufferedImage(toBufferedImage(frame)));
                // get time stamp of frame
                timeStamp = (int) Math.round(cap.get(Videoio.CAP_PROP_POS_MSEC));

                // using default partition
                if(saveFramesToFolder && extractionPartition == 1)
                {
                    Imgcodecs.imwrite(projectPath + "/" + "frame_" + generatedFrames + ".jpg", frame);
                    String outputImagePath = projectPath + "/" + "frame_" + generatedFrames + ".jpg";
                    frameExtractionMap.put(generatedFrames, Arrays.asList(outputImagePath, String.valueOf(timeStamp), videoPath));
                }

                // set partition for extraction
                if(saveFramesToFolder && extractionPartition > 1)
                {
                    if(generatedFrames % extractionPartition == 0) {
                        Imgcodecs.imwrite(projectPath + "/" + "frame_" + generatedFrames + ".jpg", frame);
                        String outputImagePath = projectPath + "/" + "frame_" + generatedFrames + ".jpg";
                        frameExtractionMap.put(frameIndex, Arrays.asList(outputImagePath, String.valueOf(timeStamp), videoPath));
                        frameIndex++;
                    }
                }
                generatedFrames++;
            }

            // to avoid unsorted write to database
            frameExtractionMap = frameExtractionMap
                    .entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));

            numOfGeneratedFrames = generatedFrames;
            frameNumberStartPoint = generatedFrames;
            log.info("Current extracted frames until time: " + timeStamp + "ms");

            cap.release();
        }
        else {
            log.info("Fail to extract frames from video");
        }
        log.info("Number of total generated frames: " + numOfGeneratedFrames);
    }

    public static int getVideoLength(String videoPath) {
        VideoCapture cap = new VideoCapture();
        cap.open(videoPath);

        int videoLength = (int) cap.get(Videoio.CAP_PROP_FRAME_COUNT) - 3; // frame start count at 0 and last frame not included
        setVideoLength(videoLength);

        return videoLength;
    }

    public static void extractSpecificFrames(String videoPath) {

        VideoCapture cap = new VideoCapture();
        cap.open(videoPath);

        int videoLength = (int) cap.get(Videoio.CAP_PROP_FRAME_COUNT);
        int framesPerSecond = (int) cap.get(Videoio.CAP_PROP_FPS);

        if(cap.isOpened())
        {
            log.info("Number of Frames: " + videoLength);
            log.info(framesPerSecond + " Frames per Second");

            Mat frame = new Mat();

//          Set extraction starting point
            cap.set(Videoio.CAP_PROP_POS_FRAMES, frameNumberStartPoint);

            while(cap.read(frame))
            {
                base64Frames.add(base64FromBufferedImage(toBufferedImage(frame)));

                if(saveFramesToFolder)
                {
                    String output = getOutputFolder(videoPath);
                    Imgcodecs.imwrite(output + "/" + "frame_" + frameNumberStartPoint +".jpg", frame);
                    // get time stamp of frame
                    timeStamp = (int) Math.round(cap.get(Videoio.CAP_PROP_POS_MSEC));
                    String outputImagePath = output + "/" + "frame_" + frameNumberStartPoint +".jpg";
                    frameExtractionMap.put(frameNumberStartPoint, Arrays.asList(outputImagePath, String.valueOf(timeStamp)));
                }

                break;

            }

            cap.release();

        }
        else {
            log.info("Fail to extract frames from video");

        }
        log.info("Extracting frames from video....");
        log.info("Number of generated frames: " + numOfGeneratedFrames);
    }

    private static String getOutputFolder(String videoPath) {
        String parentDir = Paths.get(videoPath).toFile().getAbsoluteFile().getParentFile().toString();
        File saveOutputPath = Paths.get(parentDir, "outputs").toFile();

        if(!saveOutputPath.exists()) {
            saveOutputPath.mkdir();
        }

        return saveOutputPath.toString();
    }

    public static BufferedImage toBufferedImage(Mat m) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if ( m.channels() > 1 ) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = m.channels()*m.cols()*m.rows();
        byte [] b = new byte[bufferSize];
        m.get(0,0,b); // get all the pixels
        BufferedImage image = new BufferedImage(m.cols(),m.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);

        return image;
    }

    private static String base64FromBufferedImage(BufferedImage img) {
        try
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(img, "JPG", out);
            String base64bytes = Base64.getEncoder().encodeToString(out.toByteArray());

            return "data:image/png;base64," + base64bytes;
        }
        catch (Exception e)
        {
            System.out.println("Fail to convert");
            return null;
        }
    }

//    public static List<String> getVideoFrames() {
//
//        for (Map.Entry<Integer, List<String>> entry : frameExtractionMap.entrySet()){
//            base64Frames.add(entry.getValue());
//         }
//
//        return base64Frames;
//    }

    public static void saveToProjectTable(@NonNull ProjectLoader loader)
    {
        Map <Integer, List<String>> frameMap = frameExtractionMap;

        if (frameMap.size() == 0)
        {
            PortfolioVerticle.createNewProject(loader.getProjectId());
            loader.setFileSystemStatus(FileSystemStatus.DATABASE_UPDATED);
        }
        else
        {
            Integer previousFileSysTotalUUIDSize = loader.getTotalUuidMaxLen();

            loader.resetFileSysProgress(FileSystemStatus.DATABASE_UPDATING);
            if (previousFileSysTotalUUIDSize == 1) {
                loader.setFileSysTotalUUIDSize(frameMap.size());
            } else {
                loader.setFileSysTotalUUIDSize(frameMap.size() + previousFileSysTotalUUIDSize);
            }

            for (Map.Entry<Integer, List<String>> entry : frameMap.entrySet())
            {
                String projectFullPath = loader.getProjectPath().getAbsolutePath();
                List<String> dataList = new ArrayList<>();
                String dataSubPath = StringHandler.removeFirstSlashes(FileHandler.trimPath(projectFullPath, entry.getValue().get(0)));
                String videoSubPath = StringHandler.removeFirstSlashes(FileHandler.trimPath(projectFullPath, entry.getValue().get(2)));
                dataList.add(dataSubPath);
                dataList.add(entry.getValue().get(1));
                dataList.add(videoSubPath);
                AnnotationVerticle.saveVideoDataPoint(loader, dataList, entry.getKey());
            }
        }

    }

}
