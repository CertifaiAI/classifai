/*
 * Copyright (c) 2020-2021 CertifAI Sdn. Bhd.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.classifai.util.data;

import ai.classifai.data.type.image.ImageData;
import ai.classifai.data.type.image.ImageFileType;
import ai.classifai.database.annotation.AnnotationVerticle;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.selector.status.FileSystemStatus;
import ai.classifai.util.Hash;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.exception.NotSupportedImageTypeException;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.h2.api.JavaObjectSerializer;
import org.h2.message.DbException;
import org.h2.store.Data;
import org.h2.store.DataHandler;
import org.h2.store.FileStore;
import org.h2.store.LobStorageInterface;
import org.h2.util.SmallLRUCache;
import org.h2.util.TempFileDeleter;
import org.h2.value.CompareMode;
import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Image Handler
 *
 * @author codenamewei
 */
@Slf4j
public class ImageHandler
{
//    @Override
//    public List<Data> getDataList(String projectPath) {
//        List<String> imageFiles = getValidImagesFromFolder(new File(projectPath));
//
//        return imageFiles.stream()
//                .map(path -> fileToData(path, projectPath))
//                .collect(Collectors.toList());
//    }
//
//    private Image fileToData(String filePath, String projectPath) {
//        File file = new File(filePath);
//        ImageData imageData = (ImageData) Objects.requireNonNull(getImageData(filePath));
//
//        // general data
//        // relative path
//        String relativePath = trimPath(projectPath, filePath);
//        // checksum
//        String checksum = Hash.getHash256String(file);
//        // file size
//        long fileSize = file.length();
//
//        // image data
//        // image_depth
//        int depth = imageData.getDepth();
//        // image_width
//        int width = imageData.getRawWidth();
//        // image_height
//        int height = imageData.getRawHeight();
//
//        return Image(relativePath, checksum, fileSize, depth, width, height);
//    }


    public static BufferedImage toBufferedImage(Mat matrix) {
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


    private static String getImageHeader(String input) {
        Integer lastIndex = input.length();

        Iterator<Map.Entry<String, String>> itr = ImageFileType.getBase64Header().entrySet().iterator();

        while (itr.hasNext()) {
            Map.Entry<String, String> entry = itr.next();

            String fileFormat = input.substring(lastIndex - entry.getKey().length(), lastIndex);

            if (fileFormat.equals(entry.getKey())) {
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

    public static boolean isImageReadable(File dataFullPath) {
        if ((dataFullPath.exists() == false) && (dataFullPath.length() < 5)) //length() stands for file size
        {
            log.debug(dataFullPath + " not found. Check if the data is in the corresponding path. ");

            return false;
        }

        return true;
    }

    private static BufferedImage rotateWithOrientation(BufferedImage image) throws NotSupportedImageTypeException, ImageProcessingException, IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        ImageIO.write(image, "jpeg", os);

        InputStream is = new ByteArrayInputStream(os.toByteArray());

        Metadata metadata = ImageMetadataReader.readMetadata(is);

        ImageData imgData = new ImageData.ImageDataFactory().getImageData(metadata);

        double angle = 0;

        // the image turn 270 degree
        if (imgData.getOrientation() == 8) {
            angle = (Math.PI)*(3/2);
        }
        // the image turn 180 degree
        else if (imgData.getOrientation() == 3) {
            angle = Math.PI;
        }
        // the image turn 90 degree
        else if (imgData.getOrientation() == 6) {
            angle = Math.PI/2;
        }

        double sin = Math.abs(Math.sin(angle));
        double cos = Math.abs(Math.cos(angle));

        int w = imgData.getRawWidth();
        int h = imgData.getRawHeight();

        int newW = (int) Math.floor(w * cos + h * sin);
        int newH = (int) Math.floor(h * cos + w * sin);

        int type = image.getType();
        BufferedImage result = new BufferedImage(newW, newH, type);

        Graphics2D g = result.createGraphics();

        g.translate((newW - w) / 2, (newH - h) / 2);
        g.rotate(angle,((double)w) / 2, ((double)h) / 2);
        g.drawRenderedImage(image, null );

        return result;
    }

    public static Map<String, String> getThumbNail(BufferedImage image) throws ImageProcessingException, IOException, NotSupportedImageTypeException
    {

        Map<String, String> imageData = new HashMap<>();

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        ImageIO.write(image, "png", os);

        InputStream is = new ByteArrayInputStream(os.toByteArray());

        Metadata metadata = ImageMetadataReader.readMetadata(is);

        ImageData imgData = new ImageData.ImageDataFactory().getImageData(metadata);

        Integer oriHeight;

        Integer oriWidth;

        Integer depth = imgData.getDepth();

        if (imgData.getOrientation() == 1) {

            oriHeight = imgData.getRawHeight();

            oriWidth = imgData.getRawWidth();

        } else
        {
            oriHeight = imgData.getRawHeight();

            oriWidth = imgData.getRawWidth();

            image = rotateWithOrientation(image);

        }

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

    public static String encodeFileToBase64Binary(File file) {
        try {
            FileInputStream fileInputStreamReader = new FileInputStream(file);

            byte[] bytes = new byte[(int) file.length()];

            fileInputStreamReader.read(bytes);

            String encodedfile = new String(Base64.getEncoder().encode(bytes));

            fileInputStreamReader.close();

            return getImageHeader(file.getAbsolutePath()) + encodedfile;
        } catch (Exception e) {
            log.error("Failed while converting File to base64", e);
        }

        return null;
    }

    public static boolean isImageFileValid(File file) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);

            ImageData imgData = new ImageData.ImageDataFactory().getImageData(metadata);

            if (imgData.getRawWidth() > ImageFileType.getMaxWidth() || imgData.getRawHeight() > ImageFileType.getMaxHeight()) {
                log.info("Image size bigger than maximum allowed input size. Skipped " + file);
                return false;
            }
        } catch (Exception e) {
            log.debug(String.format("Skipped %s.%n%s", file, e.getMessage()));
            return false;
        }

        return true;
    }

    public static void saveToProjectTable(@NonNull ProjectLoader loader, List<String> filesPath) {
        loader.resetFileSysProgress(FileSystemStatus.DATABASE_UPDATING);
        loader.setFileSysTotalUUIDSize(filesPath.size());

        //cloud
        if (loader.isCloud()) {
            for (int i = 0; i < filesPath.size(); ++i) {
                AnnotationVerticle.saveDataPoint(loader, filesPath.get(i), i + 1);
            }

        }
        //local file system
        else {
            for (int i = 0; i < filesPath.size(); ++i) {
                String projectFullPath = loader.getProjectPath().getAbsolutePath();
                String dataSubPath = StringHandler.removeFirstSlashes(FileHandler.trimPath(projectFullPath, filesPath.get(i)));

                AnnotationVerticle.saveDataPoint(loader, dataSubPath, i + 1);
            }

        }
    }

    public static List<String> getValidImagesFromFolder(File rootPath) {
        return FileHandler.processFolder(rootPath, ImageHandler::isImageFileValid);
    }

    private static boolean isImageUnsupported(File file) {
        return (FileHandler.isFileSupported(file.getAbsolutePath(), ImageFileType.getImageFileTypes()) && !isImageFileValid(file));
    }

    private static List<String> getUnsupportedImagesFromFolder(File rootPath) {
        return FileHandler.processFolder(rootPath, ImageHandler::isImageUnsupported);
    }


    /**
     * Iterate through project path to reflect changes
     * when create/refresh project
     * <p>
     * search through rootpath and check if list of files exists
     * scenario 1: root file missing
     * scenario 2: files missing - removed from ProjectLoader
     * scenario 3: existing uuids previously missing from current paths, but returns to the original paths
     * scenario 4: adding new files
     * scenario 5: evrything stills the same
     */
    public static boolean loadProjectRootPath(@NonNull ProjectLoader loader, boolean isNewProject) {
        if (isNewProject) {
    public static boolean loadProjectRootPath(@NonNull ProjectLoader loader)
    {
        if(Boolean.TRUE.equals(loader.getIsProjectNew()))
        {
            loader.resetFileSysProgress(FileSystemStatus.ITERATING_FOLDER);
        } else {
            //refreshing project
            loader.resetReloadingProgress(FileSystemStatus.ITERATING_FOLDER);
        }

        File rootPath = loader.getProjectPath();

        //scenario 1
        if (!rootPath.exists()) {
            loader.setSanityUuidList(new ArrayList<>());
            loader.setFileSystemStatus(FileSystemStatus.ABORTED);

            log.info("Project home path of " + rootPath.getAbsolutePath() + " is missing.");
            return false;
        }

        List<String> dataFullPathList = getValidImagesFromFolder(rootPath);
        loader.setUnsupportedImageList(getUnsupportedImagesFromFolder(rootPath));

        //Scenario 2 - 1: root path exist but all images missing
        if (dataFullPathList.isEmpty()) {
            loader.getSanityUuidList().clear();
            loader.setFileSystemStatus(FileSystemStatus.DATABASE_UPDATED);
            return false;
        }

        loader.setFileSystemStatus(FileSystemStatus.DATABASE_UPDATING);

        loader.setFileSysTotalUUIDSize(dataFullPathList.size());

        //scenario 3 - 5
        if (isNewProject) {
        if(Boolean.TRUE.equals(loader.getIsProjectNew()))
        {
            saveToProjectTable(loader, dataFullPathList);
        } else // when refreshing project folder
        {
            for (int i = 0; i < dataFullPathList.size(); ++i) {
                AnnotationVerticle.createUuidIfNotExist(loader, new File(dataFullPathList.get(i)), i + 1);
            }
        }

        return true;
    }
}
