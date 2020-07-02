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

package ai.certifai.util;

import ai.certifai.data.type.image.ImageFileType;
import ai.certifai.data.type.image.PdfHandler;
import ai.certifai.database.portfolio.PortfolioVerticle;
import ai.certifai.database.project.ProjectVerticle;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ImageHandler {

    private static final Integer FIXED_THUMBNAIL_WIDTH = 100;
    private static final Integer FIXED_THUMBNAIL_HEIGHT = 100;


    private static String getImageHeader(String input)
    {
        Integer lastIndex = input.length();

        Iterator<Map.Entry<String, String>> itr = ImageFileType.getBase64header().entrySet().iterator();

        while (itr.hasNext()) {
            Map.Entry<String, String> entry = itr.next();

            String fileFormat = input.substring(lastIndex - entry.getKey().length(), lastIndex);

            if (fileFormat.equals(entry.getKey())) {
                return entry.getValue();
            }
        }

        log.error("File format not supported");
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
            e.printStackTrace();
            log.error("error in base64FromBufferedImage");
            return "";
        }

    }

    public static boolean isImageReadable(String imagePath)
    {
        try {
            File file = new File(imagePath);

            if(file.exists() == false) return false;

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

            Integer thumbnailWidth = FIXED_THUMBNAIL_WIDTH;
            Integer thumbnailHeight = FIXED_THUMBNAIL_HEIGHT;

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
            String encodedfile = null;

            FileInputStream fileInputStreamReader = new FileInputStream(file);

            byte[] bytes = new byte[(int)file.length()];

            fileInputStreamReader.read(bytes);

            encodedfile = new String(Base64.getEncoder().encode(bytes));

            return getImageHeader(file.getAbsolutePath()) + encodedfile;
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
     * @return width, height
     */
    public static Pair<Integer, Integer> getImageSize(File file)
    {
        Integer width = 0;
        Integer height = 0;

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
            }
        }
        catch(Exception e)
        {
            log.error("Error in reading image ", e);
            return null;
        }

        return Pair.of(width, height);
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

    public static void processFile(@NonNull List<File> filesList, AtomicInteger uuidGenerator)
    {
        List<File> fileHolder = new ArrayList<>();
        List<Integer> uuidList = new ArrayList<>();

        for(File item : filesList)
        {
            String fullPath = item.getAbsolutePath();

            if(PdfHandler.isPdf(fullPath)) //handler for pdf
            {
                java.util.List<File> pdfImages = PdfHandler.savePdf2Image(fullPath);

                for(File imagePath : pdfImages)
                {
                    uuidList.add(uuidGenerator.incrementAndGet());
                    fileHolder.add(imagePath);
                }
            }
            else
            {
                uuidList.add(uuidGenerator.incrementAndGet());
                fileHolder.add(item);
            }
        }

        postProcess(fileHolder, uuidList);

    }

    public static void postProcess(@NonNull List<File> fileHolder, @NonNull List<Integer> uuidList)
    {
        if((uuidList.size() == fileHolder.size()) && (uuidList.isEmpty() == false))
        {
            //update project table
            List<Integer> uuidListVerified = ProjectVerticle.updateUUIDList(fileHolder, uuidList);

            //update portfolio table
            PortfolioVerticle.updateUUIDList(uuidListVerified);
        }
        else if(uuidList.size() != fileHolder.size())
        {
            log.error("Something is really wrong when uuidList.size() != fileHolder.size()");
        }
    }

    public static void processFolder(@NonNull File rootPath, AtomicInteger uuidGenerator)
    {
        List<File> fileHolder = new ArrayList<>();
        List<Integer> uuidList = new ArrayList<>();

        Stack<File> folderStack = new Stack<>();

        folderStack.push(rootPath);

        while(folderStack.isEmpty() != true)
        {
            File currentFolderPath = folderStack.pop();

            File[] folderList = currentFolderPath.listFiles();

            try
            {
                for(File file : folderList)
                {
                    if (file.isDirectory())
                    {
                        folderStack.push(file);
                    }
                    else
                    {
                        String absPath = file.getAbsolutePath();

                        if(isfileSupported(absPath))
                        {
                            if(PdfHandler.isPdf(absPath))
                            {
                                List<File> pdfImages = PdfHandler.savePdf2Image(absPath);

                                for(File imagePath : pdfImages)
                                {
                                    fileHolder.add(imagePath);
                                    uuidList.add(uuidGenerator.incrementAndGet());
                                }
                            }
                            else {
                                fileHolder.add(file);
                                uuidList.add(uuidGenerator.incrementAndGet());
                            }
                        }
                    }
                }
            }
            catch(Exception e)
            {
                log.info("Error occured while iterating data paths: ", e);
            }
        }

        postProcess(fileHolder, uuidList);
    }

}
