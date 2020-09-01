/*
 * Copyright (c) 2020 CertifAI
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

package ai.certifai.util.image;

import ai.certifai.data.type.image.ImageFileType;
import ai.certifai.database.portfolio.PortfolioVerticle;
import ai.certifai.database.project.ProjectVerticle;
import ai.certifai.selector.SelectorHandler;
import ai.certifai.selector.SelectorStatus;
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
import java.nio.file.Files;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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

        log.error("File format not supported");
        return null;
    }

    public static String getExtensionByStringHandling(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1)).orElse("");
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
            e.printStackTrace();
            log.error("error in base64FromBufferedImage");
            return "";
        }

    }

    public static boolean isImageReadable(String imagePath)
    {
        File file = new File(imagePath);

        if(file.exists() == false) return false;

        try {
            BufferedImage img = ImageIO.read(file);
        }
        catch(Exception e)
        {
            return false;
        }

        return true;
    }

    public static String getThumbNail(String imageAbsPath)
    {
        try
        {
            File file = new File(imageAbsPath);

            BufferedImage img  = ImageIO.read(file);

            Integer oriHeight = img.getHeight();
            Integer oriWidth = img.getWidth();

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

            return base64FromBufferedImage(resized);

        }
        catch (IOException e) {
            log.error("Failed in getting thumbnail: ", e);
            return null;
        }
    }

    public static String encodeFileToBase64Binary(File file)
    {
        try
        {
            String extension = getExtensionByStringHandling(file.getAbsolutePath());

            if(extension.equals(".tif"))
            {
                BufferedImage image = ImageIO.read(file);
                return base64FromBufferedImage(image);
            }
            else
            {
                String encodedfile = null;

                FileInputStream fileInputStreamReader = new FileInputStream(file);

                byte[] bytes = new byte[(int)file.length()];

                fileInputStreamReader.read(bytes);

                encodedfile = new String(Base64.getEncoder().encode(bytes));

                return getImageHeader(file.getAbsolutePath()) + encodedfile;
            }

        }
        catch(Exception e)
        {
            log.error("Failed while converting File to base64");
            e.printStackTrace();
        }

        return null;
    }

    /**
     *
     * @param file
     * @return width, height, depth
     */
    public static Map<String, Integer> getImageMetadata(File file)
    {
        Map<String, Integer> map = new HashMap<>();

        Integer width = 0;
        Integer height = 0;
        Integer depth = -1;

        try{
            BufferedImage bimg = ImageIO.read(file);

            if(bimg == null)
            {
                log.error("Failed in reading " + file.getAbsolutePath());
                return null;
            }
            else
            {
                width = bimg.getWidth();
                height = bimg.getHeight();

                int type = bimg.getColorModel().getColorSpace().getType();
                boolean grayscale = (type==ColorSpace.TYPE_GRAY || type==ColorSpace.CS_GRAY);

                depth = grayscale ? 1 : 3;

                map.put("width", width);
                map.put("height", height);
                map.put("depth", depth);
            }
        }
        catch(Exception e)
        {
            log.error("Error in reading image ", e);
            return null;
        }

        return map;
    }

    private static boolean isfileSupported(String file)
    {
        for(String format : ImageFileType.getImageFileTypes())
        {
            Integer beginIndex = file.length() - format.length();
            Integer endIndex = file.length();

            if(file.substring(beginIndex, endIndex).equals(format))
            {
                return true;
            }
        }
        return false;
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
            log.debug("Error: ", e);
            return false;
        }

        return true;
    }

    public static List<File> checkFile(@NonNull File file)
    {
        List<File> verifiedFilesList = new ArrayList<>();

        String currentFileFullPath = file.getAbsolutePath();

        if(isfileSupported(currentFileFullPath))
        {
            if(PdfHandler.isPdf(currentFileFullPath)) {

                java.util.List<File> pdf2ImagePaths = PdfHandler.savePdf2Image(currentFileFullPath);

                if(pdf2ImagePaths != null)
                {
                    verifiedFilesList.addAll(pdf2ImagePaths);
                }
            }
            else if(isImageFileValid(currentFileFullPath))
            {
                verifiedFilesList.add(file);
            }
        }

        return verifiedFilesList;
    }


    public static void saveToDatabase(@NonNull List<File> filesCollection, AtomicInteger uuidGenerator)
    {
        SelectorHandler.setSelectorStatus(SelectorStatus.WINDOW_CLOSE_DATABASE_UPDATING);

        List<Integer> uuidList = new ArrayList<>();

        AtomicInteger progressCounter = new AtomicInteger(0);

        for(File item : filesCollection)
        {
            Integer uuid = uuidGenerator.incrementAndGet();

            ProjectVerticle.updateUUID(uuidList, item, uuid);

            //update progress
            SelectorHandler.setProgressUpdate(SelectorHandler.getProjectNameBuffer(), new ArrayList<>(Arrays.asList(progressCounter.incrementAndGet(), filesCollection.size())));
        }

        while(true)
        {
            if((uuidList.size() == filesCollection.size()) || (!SelectorHandler.isLoaderProcessing()))
            {
                PortfolioVerticle.updateUUIDList(SelectorHandler.getProjectNameBuffer(), uuidList);
                break;
            }
        }

    }

    public static void processFile(@NonNull List<File> filesInput, AtomicInteger uuidGenerator)
    {
        List<File> validatedFilesList = new ArrayList<>();

        //SelectorHandler.setSelectorStatus(SelectorStatus.WINDOW_CLOSE_LOADING_FILES);

        for(File file : filesInput)
        {
            List<File> files = checkFile(file);
            validatedFilesList.addAll(files);
        }

        saveToDatabase(validatedFilesList, uuidGenerator);
    }

    public static void processFolder(@NonNull File rootPath, AtomicInteger uuidGenerator)
    {
        List<File> totalFilelist = new ArrayList<>();

        Stack<File> folderStack = new Stack<>();

        folderStack.push(rootPath);

        //SelectorHandler.setSelectorStatus(SelectorStatus.WINDOW_CLOSE_LOADING_FILES);

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

        saveToDatabase(totalFilelist, uuidGenerator);
    }
}
