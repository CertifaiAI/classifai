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

import ai.classifai.data.type.image.ImageFileType;
import ai.classifai.database.annotation.AnnotationVerticle;
import ai.classifai.database.annotation.bndbox.BoundingBoxVerticle;
import ai.classifai.database.annotation.seg.SegVerticle;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.selector.filesystem.FileSystemStatus;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.ProjectHandler;
import ai.classifai.util.collection.UUIDGenerator;
import ai.classifai.util.type.AnnotationType;
import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.*;

/**
 * Image Handler
 *
 * @author codenamewei
 */
@Slf4j
public class ImageHandler {

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

    public static boolean isImageReadable(String imagePath)
    {
        File file = new File(imagePath);

        if ((file.exists() == false) && (file.length() < 5)) //length() stands for file size
        {
            log.info(imagePath + " not found. Check if the data is in the corresponding path. ");

            return false;
        }

        return true;
    }

    private static int getExifOrientation(File file)
    {
        try
        {
            Metadata metadata = JpegMetadataReader.readMetadata(file);
            Directory dir= metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);

            return dir.getInt(274);
        }
        catch (Exception e)
        {
            return 0;
        }
    }

    private static BufferedImage rotate(BufferedImage image, double angle)
    {
        double sin = Math.abs(Math.sin(angle));
        double cos = Math.abs(Math.cos(angle));

        int w = image.getWidth();
        int h = image.getHeight();

        int newW = (int) Math.floor(w * cos + h * sin);
        int newH = (int) Math.floor(h * cos + w * sin);

        int type = image.getType();
        BufferedImage result = new BufferedImage(newW, newH, type);

        Graphics2D g = result.createGraphics();

        g.translate((newW - w) / 2, (newH - h) / 2);
        g.rotate(angle,((double)w) / 2, ((double)h) / 2);
        g.drawRenderedImage(image, null);

        return result;
    }

    private static BufferedImage rotateWithOrientation(BufferedImage img, int orientation)
    {
        double angle = 0;

        if (orientation == 8) angle = -Math.PI/2;
        else if (orientation == 3) angle = Math.PI;
        else if (orientation == 6) angle = Math.PI/2;

        return rotate(img,angle);
    }

    private static int getHeight(BufferedImage img, int orientation)
    {
        if (orientation == 8 || orientation == 6)
        {
            return img.getWidth();
        }
        return img.getHeight();
    }

    private static int getWidth(BufferedImage img, int orientation)
    {
        if (orientation == 8 || orientation == 6)
        {
            return img.getHeight();
        }
        return img.getWidth();
    }


    public static Map<String, String> getThumbNail(String imageAbsPath)
    {
        try
        {
            File file = new File(imageAbsPath);

            BufferedImage img  = ImageIO.read(file);
            int orientation = getExifOrientation(file);

            Integer oriHeight = getHeight(img, orientation);
            Integer oriWidth = getWidth(img, orientation);

            //rotate for thumbnail generation
            img = rotateWithOrientation(img, orientation);

            int type = img.getColorModel().getColorSpace().getType();
            boolean grayscale = (type == ColorSpace.TYPE_GRAY || type == ColorSpace.CS_GRAY);

            Integer depth = grayscale ? 1 : 3;

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

            Image tmp = img.getScaledInstance(thumbnailWidth, thumbnailHeight, Image.SCALE_SMOOTH);
            BufferedImage resized = new BufferedImage(thumbnailWidth, thumbnailHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = resized.createGraphics();
            g2d.drawImage(tmp, 0, 0, null);
            g2d.dispose();


            Map<String, String> imageData = new HashMap<>();
            imageData.put(ParamConfig.getImgDepth(), Integer.toString(depth));
            imageData.put(ParamConfig.getImgOriHParam(), Integer.toString(oriHeight));
            imageData.put(ParamConfig.getImgOriWParam(), Integer.toString(oriWidth));
            imageData.put(ParamConfig.getBase64Param(), base64FromBufferedImage(resized));

            return imageData;
        }
        catch (IOException e)
        {
            log.debug("Failed in getting thumbnail for path " + imageAbsPath, e);
            return null;
        }
    }

    public static String encodeFileToBase64Binary(File file)
    {
        try
        {
            String encodedfile = null;

            FileInputStream fileInputStreamReader = new FileInputStream(file);

            byte[] bytes = new byte[(int)file.length()];

            fileInputStreamReader.read(bytes);

            encodedfile = new String(Base64.getEncoder().encode(bytes));

            return getImageHeader(file.getAbsolutePath()) + encodedfile;
        }
        catch (Exception e)
        {
            log.error("Failed while converting File to base64", e);
        }

        return null;
    }


    private static boolean isImageFileValid(String file)
    {
        try
        {
            File filePath = new File(file);

            BufferedImage bimg = ImageIO.read(filePath);

            if (bimg == null)
            {
                log.info("Failed in reading. Skipped " + filePath.getAbsolutePath());
                return false;
            }
            else if ((bimg.getWidth() > ImageFileType.getMaxWidth()) || (bimg.getHeight() > ImageFileType.getMaxHeight()))
            {
                log.info("Image size bigger than maximum allowed input size. Skipped " + filePath.getAbsolutePath());
                return false;
            }
        }
        catch (Exception e)
        {
            log.debug("Error in checking if image file valid - " + file, e);
            return false;
        }

        return true;
    }

    public static List<File> checkFile(@NonNull File file)
    {
        List<File> verifiedFilesList = new ArrayList<>();

        String currentFileFullPath = file.getAbsolutePath();

        if (FileHandler.isfileSupported(currentFileFullPath, ImageFileType.getImageFileTypes()))
        {
            if (isImageFileValid(currentFileFullPath))
            {
                verifiedFilesList.add(file);
            }
        }

        return verifiedFilesList;
    }


    public static void saveToDatabase(@NonNull String projectID, @NonNull List<File> filesCollection)
    {
        ProjectLoader loader = ProjectHandler.getProjectLoader(projectID);

        loader.resetFileSysProgress(FileSystemStatus.WINDOW_CLOSE_DATABASE_UPDATING);
        loader.setFileSysTotalUUIDSize(filesCollection.size());

        Integer annotationTypeInt = loader.getAnnotationType();

        if (annotationTypeInt.equals(AnnotationType.BOUNDINGBOX.ordinal()))
        {
            for (int i = 0; i < filesCollection.size(); ++i)
            {
                String uuid = UUIDGenerator.generateUUID();

                AnnotationVerticle.updateUUID(BoundingBoxVerticle.getJdbcPool(), projectID, filesCollection.get(i), uuid, i + 1);
            }
        }
        else if (annotationTypeInt.equals(AnnotationType.SEGMENTATION.ordinal()))
        {
            for (int i = 0; i < filesCollection.size(); ++i)
            {
                String uuid = UUIDGenerator.generateUUID();
            
                AnnotationVerticle.updateUUID(SegVerticle.getJdbcPool(), projectID, filesCollection.get(i), uuid, i + 1);
            }
        }
    }
    public static void processFile(@NonNull String projectID, @NonNull List<File> filesInput)
    {
        List<File> validatedFilesList = new ArrayList<>();

        for (File file : filesInput)
        {
            List<File> files = checkFile(file);
            validatedFilesList.addAll(files);
        }

        saveToDatabase(projectID, validatedFilesList);
    }

    public static void processFolder(@NonNull String projectID, @NonNull File rootPath)
    {
        List<File> totalFilelist = new ArrayList<>();

        ProjectLoader loader = ProjectHandler.getProjectLoader(projectID);

        String[] fileExtension = ImageFileType.getImageFileTypes();
        List<File> dataList = FileHandler.processFolder(rootPath, fileExtension);

        if (dataList.isEmpty())
        {
            loader.resetFileSysProgress(FileSystemStatus.WINDOW_CLOSE_DATABASE_NOT_UPDATED);
            return;
        }

        Stack<File> folderStack = new Stack<>();
        folderStack.push(rootPath);

        while (folderStack.isEmpty() != true)
        {
            File currentFolderPath = folderStack.pop();

            File[] folderList = currentFolderPath.listFiles();

            for (File file : folderList)
            {
                if (file.isDirectory())
                {
                    folderStack.push(file);
                }
                else
                {
                    List<File> files = checkFile(file);
                    totalFilelist.addAll(files);
                }
            }
        }

        saveToDatabase(projectID, totalFilelist);
    }

    /*
    search through rootpath and check if list of files exists
    scenario 1: root file missing
    scenario 2: files missing - removed from ProjectLoader
    scenario 3: existing uuids previously missing from current paths, but returns to the original paths
    scenario 4: adding new files
    scenario 5: evrything stills the same
    */
    public static void recheckProjectRootPath(@NonNull String projectID)
    {
        ProjectLoader loader = ProjectHandler.getProjectLoader(projectID);

        loader.resetReloadingProgress(FileSystemStatus.WINDOW_CLOSE_LOADING_FILES);

        File rootPath = new File(loader.getProjectPath());
        //scenario 1
        if(!rootPath.exists())
        {
            loader.setSanityUUIDList(new ArrayList<>());
            loader.setFileSystemStatus(FileSystemStatus.WINDOW_CLOSE_DATABASE_UPDATED);

            log.info("Project home path of " + rootPath.getAbsolutePath() + " is missing.");
            return;
        }

        String[] fileExtension = ImageFileType.getImageFileTypes();
        List<File> dataList = FileHandler.processFolder(rootPath, fileExtension);

        //Scenario 2 - 1: root path exist but all images missing
        if(dataList.isEmpty())
        {
            loader.getSanityUUIDList().clear();
            loader.setFileSystemStatus(FileSystemStatus.WINDOW_CLOSE_DATABASE_UPDATED);
            return;
        }

        loader.setFileSystemStatus(FileSystemStatus.WINDOW_CLOSE_DATABASE_UPDATING);

        loader.setFileSysTotalUUIDSize(dataList.size());

        //scenario 2 - 5

        for(int i = 0; i < dataList.size(); ++i)
        {
            if (loader.getAnnotationType().equals(AnnotationType.BOUNDINGBOX.ordinal()))
            {
                BoundingBoxVerticle.createUUIDIfNotExist(BoundingBoxVerticle.getJdbcPool(), projectID, dataList.get(i), i + 1);
            }
            else if (loader.getAnnotationType().equals(AnnotationType.SEGMENTATION.ordinal()))
            {
                //TODO
            }
        }
    }
}
