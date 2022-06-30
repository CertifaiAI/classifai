package ai.classifai.core.data.handler;

import ai.classifai.core.data.type.image.ImageData;
import ai.classifai.core.data.type.image.ImageFileType;
import ai.classifai.core.loader.ProjectLoader;
import ai.classifai.core.properties.image.ImageDTO;
import ai.classifai.core.utility.ParamConfig;
import ai.classifai.core.utility.handler.FileHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;

/**
 * Image Handler
 *
 * @author codenamewei
 */
@Slf4j
public class ImageHandler {
    @Setter
    @Getter
    private int currentAddedImages = 0;

    @Setter
    @Getter
    private int totalImagesToBeAdded = 0;

    public static BufferedImage toBufferedImage(Mat matrix)
    {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (matrix.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = matrix.channels() * matrix.cols() * matrix.rows();
        byte[] buffer = new byte[bufferSize];
        matrix.get(0, 0, buffer); // get all the pixels
        BufferedImage image = new BufferedImage(matrix.cols(), matrix.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);
        return image;
    }

    private static String getImageHeader(String input)
    {
        Integer lastIndex = input.length();

        Iterator<Map.Entry<String, String>> itr = ImageFileType.getBase64Header().entrySet().iterator();

        while (itr.hasNext())
        {
            Map.Entry<String, String> entry = itr.next();

            String fileFormat = input.substring(lastIndex - entry.getKey().length(), lastIndex);

            if (fileFormat.equals(entry.getKey()))
            {
                return entry.getValue();
            }
        }

        log.debug("File format not supported");

        return null;
    }

    private static String base64FromBufferedImage(BufferedImage img)
    {
        try
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(img, "PNG", out);
            byte[] bytes = out.toByteArray();
            String base64bytes = Base64.getEncoder().encodeToString(bytes);
            String src = "data:image/png;base64," + base64bytes;

            return src;
        }
        catch (Exception e)
        {
            log.debug("Error in converting BufferedImage into base64: ", e);
            return "";
        }
    }

    public static boolean isImageReadable(File dataFullPath)
    {
        if ((dataFullPath.exists() == false) && (dataFullPath.length() < 5)) //length() stands for file size
        {
            log.debug(dataFullPath + " not found. Check if the data is in the corresponding path. ");

            return false;
        }

        return true;
    }

    private static BufferedImage rotateWithOrientation(BufferedImage image) throws Exception
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        ImageData imgData = ImageData.getImageData(out.toByteArray());

        double angle = imgData.getAngle();

        double sin = Math.abs(Math.sin(angle));
        double cos = Math.abs(Math.cos(angle));

        int w = imgData.getWidth();
        int h = imgData.getHeight();

        int newW = (int) Math.floor(w * cos + h * sin);
        int newH = (int) Math.floor(h * cos + w * sin);
        int type = image.getType();

        BufferedImage result = new BufferedImage(newW, newH, type);

        Graphics2D g = result.createGraphics();

        g.translate((newW - w) / 2, (newH - h) / 2);
        g.rotate(angle,(double) w / 2, (double) h / 2);
        g.drawRenderedImage(image, null);

        return result;
    }

    public static Map<String, String> getImageMetaData(File imageFile) throws Exception {
        int oriWidth = 0;
        int oriHeight = 0;
        int depth = 0;

        ImageData imgData = ImageData.getImageData(imageFile);

        if(imgData != null)
        {
            oriWidth = imgData.getWidth();
            oriHeight = imgData.getHeight();
            depth = imgData.getDepth();
        }

        Mat imageMat  = Imgcodecs.imread(imageFile.getAbsolutePath());
        BufferedImage image = ImageHandler.toBufferedImage(imageMat);

        return getThumbNailAttributes(image, oriWidth, oriHeight, depth);
    }

    private static Map<String, String> getThumbNailAttributes(BufferedImage image, int oriWidth, int oriHeight, int depth)
    {
        Map<String, String> imageData = new HashMap<>();

        Integer thumbnailWidth = ImageFileType.getFixedThumbnailWidth();
        Integer thumbnailHeight = ImageFileType.getFixedThumbnailHeight();

        if (oriHeight > oriWidth)
        {
            thumbnailWidth =  thumbnailHeight * oriWidth / oriHeight;
        }
        else
        {
            thumbnailHeight = thumbnailWidth * oriHeight / oriWidth;
        }

        Image tmp = image.getScaledInstance(thumbnailWidth, thumbnailHeight, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(thumbnailWidth, thumbnailHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        imageData.put(ParamConfig.getImgDepth(), Integer.toString(depth));
        imageData.put(ParamConfig.getImgOriHParam(), Integer.toString(oriHeight));
        imageData.put(ParamConfig.getImgOriWParam(), Integer.toString(oriWidth));
        imageData.put(ParamConfig.getBase64Param(), base64FromBufferedImage(resized));

        return imageData;
    }

    public static String encodeFileToBase64Binary(File file)
    {
        try
        {
            FileInputStream fileInputStreamReader = new FileInputStream(file);

            byte[] bytes = new byte[(int)file.length()];

            fileInputStreamReader.read(bytes);

            String encodedfile = new String(Base64.getEncoder().encode(bytes));

            fileInputStreamReader.close();

            return getImageHeader(file.getAbsolutePath()) + encodedfile;
        }
        catch (Exception e)
        {
            log.error("Failed while converting File to base64", e);
        }

        return null;
    }

    public static boolean isImageFileValid(File file)
    {
        try
        {
            ImageData imgData = ImageData.getImageData(file);

            if (imgData.getWidth() > ImageFileType.getMaxWidth() || imgData.getHeight() > ImageFileType.getMaxHeight())
            {
                log.info("Image size bigger than maximum allowed input size. Skipped " + file);
                return false;
            }
            else if (imgData.isAnimation()) {
                log.info("The image is animated and not supported ");
                return false;
            }
        }
        catch (Exception e)
        {
            log.debug(String.format("Skipped %s.%n%s", file, e.getMessage()));
            return false;
        }

        return true;
    }

    public static List<String> getValidImagesFromFolder(File rootPath)
    {
        return FileHandler.processFolder(rootPath, ImageHandler::isImageFileValid);
    }

    private static boolean isImageUnsupported(File file)
    {
        return (FileHandler.isFileSupported(file.getAbsolutePath(), ImageFileType.getImageFileTypes()) && !isImageFileValid(file));
    }

    public static List<String> getUnsupportedImagesFromFolder(File rootPath)
    {
        return FileHandler.processFolder(rootPath, ImageHandler::isImageUnsupported);
    }

    public void addImageToProjectFolder(List<String> imageNameList, List<String> imageBase64List, File projectPath,
                                        List<String> currentFolderFiles)
    {
        setTotalImagesToBeAdded(imageNameList.size());

        for(int i = 0; i < imageNameList.size(); i++)
        {
            try
            {
                //decode image base64 string into image
                byte[] decodedBytes = Base64.getDecoder().decode(imageBase64List.get(i).split("base64,")[1]);
                File imageFile = new File(projectPath.getAbsolutePath() + File.separator + imageNameList.get(i));
                setCurrentAddedImages(i + 1); //to make count start at 1

                if(!currentFolderFiles.contains(imageFile.getAbsolutePath()))
                {
                    FileUtils.writeByteArrayToFile(imageFile, decodedBytes);
                    log.debug(imageFile.getName() + " is added to project folder " + projectPath.getName());
                }
                else
                {
                    log.debug(imageFile.getName() + " is exist in current folder");
                    log.debug("Operation add " + imageFile.getName() + " to project folder " + projectPath.getName() + " aborted");
                }
            }
            catch (IOException e)
            {
                log.info("Fail to convert Base64 String to Image file");
                return;
            }
        }
    }

    public static void getExampleImage(File projectPath) throws IOException {
        // Get example image from metadata
        String exampleSrcFileName = "/classifai_overview.png";
        String exampleImgFileName = "example_img.png";

        BufferedImage srcImg = ImageIO.read(
                Objects.requireNonNull(ProjectLoader.class.getResource(exampleSrcFileName)));
        String destImgFileStr = Paths.get(projectPath.getAbsolutePath(), exampleImgFileName).toString();
        ImageIO.write(srcImg, "png", new File(destImgFileStr));
        log.info("Empty folder. Example image added.");
    }

    public static ImageDTO getAnnotation(File imageFile, String projectId) throws Exception {
        Map<String, String> imageMetaData = getImageMetaData(imageFile);
        return ImageDTO.builder()
                .projectId(projectId)
                .imgOriW(Integer.parseInt(imageMetaData.get(ParamConfig.getImgOriWParam())))
                .imgOriH(Integer.parseInt(imageMetaData.get(ParamConfig.getImgOriHParam())))
                .imgDepth(Integer.parseInt(imageMetaData.get(ParamConfig.getImgDepth())))
                .fileSize(FileUtils.sizeOf(imageFile))
                .imgPath(imageFile.getName())
                .build();
    }
}
