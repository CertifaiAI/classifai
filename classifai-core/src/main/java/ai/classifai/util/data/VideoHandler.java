package ai.classifai.util.data;

import ai.classifai.database.annotation.AnnotationDB;
import ai.classifai.database.portfolio.PortfolioDB;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.ui.enums.FileSystemStatus;
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
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class VideoHandler {
    private static int currentFrameIndex = 0;
    @Getter @Setter private static boolean isFinishedExtraction = false;
    @Getter private static int timeStamp = 0;
    @Getter private static final int batchSize = 50;
    @Setter private static int frameNumberStartPoint = 0;
    @Getter @Setter private static int videoLength = 0;
    @Getter private static int numOfGeneratedFrames = 0;
    @Getter private static Map<Integer, List<String>> frameExtractionMap = new LinkedHashMap<>();
    @Getter private static final List<String> base64Frames = new ArrayList<>();

    public static void extractFrames(String videoPath, Integer extractionPartition, String projectPath, Double startTime, Double endTime) {

        VideoCapture cap = new VideoCapture();
        cap.open(videoPath);

        if(extractionPartition == null) {
            extractionPartition = 1;
        }

        int generatedFrames = 0;
        float framesPerSecond = (float) cap.get(Videoio.CAP_PROP_FPS);
        float videoLength = (float) cap.get(Videoio.CAP_PROP_FRAME_COUNT);
        float videoDuration = videoLength / framesPerSecond * 1000;
        int extractAtFrameIndex = (int) Math.floor((startTime/videoDuration) * videoLength);
        int endIndex = (int) Math.floor((endTime/videoDuration) * videoLength);

        cap.set(Videoio.CAP_PROP_POS_FRAMES, extractAtFrameIndex);

        frameExtractionMap.clear();
        if(cap.isOpened())
        {
            Mat frame = new Mat();

            while(cap.read(frame)) //the last frame of the movie will be invalid. check for it !
            {
                // get time stamp of frame
                timeStamp = (int) Math.round(cap.get(Videoio.CAP_PROP_POS_MSEC));

                // using default partition
                if(extractionPartition == 1)
                {
                    log.info("current time stamp is: " + timeStamp);
                    String outputImagePath = projectPath + "/" + "frame_" + currentFrameIndex + ".jpg";
                    Imgcodecs.imwrite(outputImagePath, frame);
                    frameExtractionMap.put(currentFrameIndex, Arrays.asList(outputImagePath, String.valueOf(timeStamp), videoPath));
                }
                // using selected partition for extraction
                else
                {
                    if(generatedFrames % extractionPartition == 0) {
                        log.info("current time stamp is: " + timeStamp);
                        String outputImagePath = projectPath + "/" + "frame_" + currentFrameIndex + ".jpg";
                        Imgcodecs.imwrite(outputImagePath, frame);
                        frameExtractionMap.put(currentFrameIndex, Arrays.asList(outputImagePath, String.valueOf(timeStamp), videoPath));
                        currentFrameIndex++;
                    }
                }
                generatedFrames++;
            }

            numOfGeneratedFrames = generatedFrames;
            isFinishedExtraction = generatedFrames >= getVideoLength(videoPath);

            // to avoid unsorted write to database
            frameExtractionMap = frameExtractionMap
                    .entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));

            cap.release();
        }
        else {
            log.info("Fail to extract frames from video");
        }

        if (extractionPartition > 1) {
            log.info("Total generated frames: " + currentFrameIndex);
            log.info("Extracted frames from time: " + startTime + " until " + endTime);
        } else {
            log.info("Total generated frames: " + generatedFrames);
            log.info("Extracted frames from time: " + startTime + " until " + endTime);
        }
    }

    public static int getVideoLength(String videoPath) {
        VideoCapture cap = new VideoCapture();
        cap.open(videoPath);

        int videoLength = (int) cap.get(Videoio.CAP_PROP_FRAME_COUNT) - 2; // frame start count at 0 and last frame not included
        setVideoLength(videoLength);

        return videoLength;
    }

    public static int getFramePerSeconds(String videoPath) {
        VideoCapture cap = new VideoCapture();
        cap.open(videoPath);
        return (int) cap.get(Videoio.CAP_PROP_FPS);
    }

    public static String getVideoDuration(String videoPath) {
        VideoCapture cap = new VideoCapture();
        cap.open(videoPath);

        int videoLength = (int) cap.get(Videoio.CAP_PROP_FRAME_COUNT) - 2;
        int framePerSecond = (int) cap.get(Videoio.CAP_PROP_FPS);
        int calcVideoDuration = videoLength/framePerSecond;

        return String.format("%02d:%02d:%02d",(calcVideoDuration/3600), ((calcVideoDuration % 3600)/60), (calcVideoDuration % 60));
    }

    private static String timeConversion(Long timeStamp) {
        Duration time = Duration.ofMillis(timeStamp);
        return String.format("%02d:%02d:%02d",
                time.toHours(), time.toMinutesPart(), time.toSecondsPart());
    }

    public static void extractSpecificFrames(String videoPath, String projectPath, Double currentTime) {
        log.info("Extracting frames from video....");
        log.info("Saving output to " + projectPath);

        VideoCapture cap = new VideoCapture();
        cap.open(videoPath);

        float videoLength = (float) cap.get(Videoio.CAP_PROP_FRAME_COUNT);
        float framesPerSecond = (float) cap.get(Videoio.CAP_PROP_FPS);
        float videoDuration = (videoLength / framesPerSecond) * 1000;
        int frameIndex = (int) Math.floor((currentTime/videoDuration) * videoLength);

        if(cap.isOpened())
        {
            Mat frame = new Mat();
            cap.set(Videoio.CAP_PROP_POS_FRAMES, frameIndex);

            while(cap.read(frame))
            {
                base64Frames.add(base64FromBufferedImage(toBufferedImage(frame)));
                String outputImagePath = projectPath + "/" + "frame_" + currentFrameIndex + ".jpg";
                Imgcodecs.imwrite(outputImagePath, frame);
                timeStamp = (int) Math.round(cap.get(Videoio.CAP_PROP_POS_MSEC));
                frameExtractionMap.put(currentFrameIndex, Arrays.asList(outputImagePath, String.valueOf(timeStamp), videoPath));
                currentFrameIndex += 1;
                numOfGeneratedFrames += 1;

                break;
            }
            cap.release();
        }
        else {
            log.info("Fail to extract frames from video");
        }
    }

    public static void extractFramesForSelectedTimeRange(String videoPath, Integer partition, String projectPath, Double startTime, Double endTime) {
        log.info("Extracting frames from video....");
        log.info("Saving output to " + projectPath);

        VideoCapture cap = new VideoCapture();
        cap.open(videoPath);

        int framesPerSecond = (int) cap.get(Videoio.CAP_PROP_FPS);
        float timeInterval = (float) 1 / framesPerSecond * 1000;
        int generatedFrames = 0;
        int frameCount = 0;

        for (double i = startTime; i <= endTime; i += timeInterval) {
            if(partition > 1) {
                if(generatedFrames % partition == 0) {
                    extractSpecificFrames(videoPath, projectPath, i);
                    frameCount++;
                }
            } else {
                extractSpecificFrames(videoPath, projectPath, i);
                frameCount++;
            }
            generatedFrames++;
        }

        VideoHandler.setFinishedExtraction(true);
        log.info("Extracted frames from time: " + timeConversion(startTime.longValue()) + " until " + timeConversion(endTime.longValue()));
        log.info("Total generated frames: " + frameCount);
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

    private static void saveToProjectTable(@NonNull ProjectLoader loader, Map<Integer, List<String>> frameExtractionMap, PortfolioDB portfolioDB, AnnotationDB annotationDB)
    {
        if (frameExtractionMap.size() == 0)
        {
            portfolioDB.createNewProject(loader.getProjectId());
            loader.setFileSystemStatus(FileSystemStatus.DATABASE_UPDATED);
        }
        else
        {
            loader.resetFileSysProgress(FileSystemStatus.DATABASE_UPDATING);
            loader.setFileSysTotalUUIDSize(numOfGeneratedFrames);

            for (Map.Entry<Integer, List<String>> entry : frameExtractionMap.entrySet())
            {
                String projectFullPath = loader.getProjectPath().getAbsolutePath();
                List<String> dataList = new ArrayList<>();
                String dataSubPath = StringHandler.removeFirstSlashes(FileHandler.trimPath(projectFullPath, entry.getValue().get(0)));
                String videoSubPath = StringHandler.removeFirstSlashes(FileHandler.trimPath(projectFullPath, entry.getValue().get(2)));
                dataList.add(dataSubPath);
                dataList.add(entry.getValue().get(1));
                dataList.add(videoSubPath);
                annotationDB.saveVideoDataPoint(loader, dataList, entry.getKey());
            }

            frameExtractionMap.clear();

        }

    }

    public static boolean loadVideoProjectRootPath(@NonNull ProjectLoader loader, PortfolioDB portfolioDB, AnnotationDB annotationDB)
    {
        if(loader.getIsProjectNew())
        {
            loader.resetFileSysProgress(FileSystemStatus.ITERATING_FOLDER);
        }

        File rootPath = loader.getProjectPath();

        if(!rootPath.exists())
        {
            loader.setSanityUuidList(new ArrayList<>());
            loader.setFileSystemStatus(FileSystemStatus.ABORTED);

            log.info("Project home path of " + rootPath.getAbsolutePath() + " is missing.");
            return false;
        }

        loader.setUnsupportedImageList(ImageHandler.getUnsupportedImagesFromFolder(rootPath));
        VideoHandler.saveToProjectTable(loader, frameExtractionMap, portfolioDB, annotationDB);

        return true;
    }

}
