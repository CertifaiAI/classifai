/*
 * Copyright (c) 2020 CertifAI Sdn. Bhd.
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
import ai.classifai.database.annotation.bndbox.BoundingBoxDbQuery;
import ai.classifai.database.annotation.bndbox.BoundingBoxVerticle;
import ai.classifai.database.annotation.seg.SegDbQuery;
import ai.classifai.database.annotation.seg.SegVerticle;
import ai.classifai.database.portfolio.PortfolioVerticle;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.selector.filesystem.FileSystemStatus;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.ProjectHandler;
import ai.classifai.util.type.AnnotationType;
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
import java.util.concurrent.atomic.AtomicInteger;

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

        Iterator<Map.Entry<String, String>> itr = ImageFileType.getBase64header().entrySet().iterator();

        while (itr.hasNext())
        {
            Map.Entry<String, String> entry = itr.next();

            String fileFormat = input.substring(lastIndex - entry.getKey().length(), lastIndex);

            if (fileFormat.equals(entry.getKey())) {
                return entry.getValue();
            }
        }

        log.debug("File format not supported");

        return null;
    }

    private static String base64FromBufferedImage(BufferedImage img) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(img, "PNG", out);
            byte[] bytes = out.toByteArray();
            String base64bytes = Base64.getEncoder().encodeToString(bytes);
            String src = "data:image/png;base64," + base64bytes;

            return src;
        } catch (Exception e) {
            log.debug("Error in converting BufferedImage into base64: ", e);
            return "";
        }

    }

    public static boolean isImageReadable(String imagePath)
    {
        File file = new File(imagePath);

        if((file.exists() == false) && (file.length() < 5)) //length() stands for file size
        {
            return false;
        }

        return true;
    }


    public static Map<String, String> getThumbNail(String imageAbsPath)
    {
        try
        {
            File file = new File(imageAbsPath);

            BufferedImage img  = ImageIO.read(file);

            Integer oriHeight = img.getHeight();
            Integer oriWidth = img.getWidth();

            int type = img.getColorModel().getColorSpace().getType();
            boolean grayscale = (type == ColorSpace.TYPE_GRAY || type == ColorSpace.CS_GRAY);

            Integer depth = grayscale ? 1 : 3;

            Integer thumbnailWidth = ImageFileType.getFixedThumbnailWidth();
            Integer thumbnailHeight = ImageFileType.getFixedThumbnailHeight();

            if(oriHeight > oriWidth)
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
            imageData.put(ParamConfig.getImageDepth(), Integer.toString(depth));
            imageData.put(ParamConfig.getImageORIHParam(), Integer.toString(oriHeight));
            imageData.put(ParamConfig.getImageORIWParam(), Integer.toString(oriWidth));
            imageData.put(ParamConfig.getBase64Param(), base64FromBufferedImage(resized));

            return imageData;

        }
        catch (IOException e) {
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
        catch(Exception e)
        {
            log.error("Failed while converting File to base64", e);
        }

        return null;
    }


    private static boolean isImageFileValid(String file)
    {
        try {
            File filePath = new File(file);

            BufferedImage bimg = ImageIO.read(filePath);

            if (bimg == null)
            {
                log.info("Failed in reading. Skipped " + filePath.getAbsolutePath());
                return false;
            }
            else if((bimg.getWidth() > ImageFileType.getMaxWidth()) || (bimg.getHeight() > ImageFileType.getMaxHeight()))
            {
                log.info("Image size bigger than maximum allowed input size. Skipped " + filePath.getAbsolutePath());
                return false;
            }
        }
        catch(Exception e)
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

        if(FileHandler.isfileSupported(currentFileFullPath, ImageFileType.getImageFileTypes()))
        {
            if(isImageFileValid(currentFileFullPath))
            {
                verifiedFilesList.add(file);
            }
        }

        return verifiedFilesList;
    }


    public static void saveToDatabase(@NonNull Integer projectID, @NonNull List<File> filesCollection)
    {
        ProjectLoader loader = ProjectHandler.getProjectLoader(projectID);
        AtomicInteger uuidGenerator = new AtomicInteger(loader.getUuidGeneratorSeed());

        //update Portfolio Verticle Generator Seed
        Integer uuidNewSeed = filesCollection.size() + loader.getUuidGeneratorSeed();
        PortfolioVerticle.updateUUIDGeneratorSeed(projectID, uuidNewSeed);

        loader.reset(FileSystemStatus.WINDOW_CLOSE_DATABASE_UPDATING);
        loader.setFileSysTotalUUIDSize(filesCollection.size());

        Integer annotationTypeInt = loader.getAnnotationType();

        if(annotationTypeInt.equals(AnnotationType.BOUNDINGBOX.ordinal()))
        {
            for(int i = 0; i < filesCollection.size(); ++i)
            {
                Integer uuid = uuidGenerator.incrementAndGet();

                BoundingBoxVerticle.updateUUID(BoundingBoxVerticle.getJdbcClient(), BoundingBoxDbQuery.createData(), projectID, filesCollection.get(i), uuid, i + 1);
            }
        }
        else if (annotationTypeInt.equals(AnnotationType.SEGMENTATION.ordinal()))
        {
            for(int i = 0; i < filesCollection.size(); ++i)
            {
                Integer uuid = uuidGenerator.incrementAndGet();

                SegVerticle.updateUUID(SegVerticle.getJdbcClient(), SegDbQuery.createData(), projectID, filesCollection.get(i), uuid, i + 1);

            }
        }
    }
    public static void processFile(@NonNull Integer projectID, @NonNull List<File> filesInput)
    {
        List<File> validatedFilesList = new ArrayList<>();

        for(File file : filesInput)
        {
            List<File> files = checkFile(file);
            validatedFilesList.addAll(files);
        }

        saveToDatabase(projectID, validatedFilesList);
    }

    public static void processFolder(@NonNull Integer projectID, @NonNull File rootPath)
    {
        List<File> totalFilelist = new ArrayList<>();
        Stack<File> folderStack = new Stack<>();
        ProjectLoader loader = ProjectHandler.getProjectLoader(projectID);
        String[] fileExtension = ImageFileType.getImageFileTypes();
        java.util.List<File> checkFileFormat = FileHandler.processFolder(rootPath, fileExtension);

        folderStack.push(rootPath);

        if(checkFileFormat.isEmpty())
        {
            loader.reset(FileSystemStatus.WINDOW_CLOSE_DATABASE_NOT_UPDATED);
            return;
        }

        while(folderStack.isEmpty() != true)
        {
            File currentFolderPath = folderStack.pop();

            File[] folderList = currentFolderPath.listFiles();

            for(File file : folderList)
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
}
