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
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;

@Slf4j
public class ImageUtils {
    private static Map base64header;

    private static final Integer FIXED_THUMBNAIL_WIDTH = 100;
    private static final Integer FIXED_THUMBNAIL_HEIGHT = 100;

    static {
        base64header = new HashMap();
        base64header.put("jpg", "data:image/jpeg;base64,");
        base64header.put("jpeg", "data:image/jpeg;base64,");
        base64header.put("png", "data:image/png;base64,");
    }

    private static String getImageHeader(String input) {
        Integer lastIndex = input.length();

        Iterator<Map.Entry<String, String>> itr = base64header.entrySet().iterator();

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
    public static ImmutablePair<Integer, Integer> getImageSize(File file)
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

        return new ImmutablePair(width, height);
    }

    public static void generateUUID(java.util.List<File> filesList)
    {
        java.util.List<File> tempFileHolder = new ArrayList<>();

        for(File item : filesList)
        {
            String fullPath = item.getAbsolutePath();

            if(PdfHandler.isPdf(fullPath)) //handler for pdf
            {
                java.util.List<File> pdfImages = PdfHandler.savePdf2Image(fullPath);

                if(pdfImages.isEmpty() == false)
                {
                    for(File imagePath : pdfImages)
                    {
                        uuidList.add(generateUUID());
                        tempFileHolder.add(imagePath);
                    }
                }
            }
            else
            {
                uuidList.add(generateUUID());
                tempFileHolder.add(item);
            }
        }

        fileHolder = tempFileHolder;
    }

    public static void generateUUIDwithIteration(@NonNull File rootDataPath)
    {
        fileHolder = new ArrayList<>();
        Stack<File> folderStack = new Stack<>();

        folderStack.push(rootDataPath);

        //need to fix this
        java.util.List<String> acceptableFileFormats = new ArrayList<>(Arrays.asList(ImageFileType.getImageFileTypes()));

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

                        for (String allowedFormat : acceptableFileFormats)
                        {
                            if(absPath.length() > allowedFormat.length())
                            {
                                String currentFormat = absPath.substring(absPath.length()  - allowedFormat.length());

                                if(currentFormat.equals(PdfHandler.getPDFFORMAT()))
                                {
                                    List<File> pdfImages = PdfHandler.savePdf2Image(absPath);

                                    if(pdfImages.isEmpty() == false)
                                    {
                                        for(File imagePath : pdfImages)
                                        {
                                            fileHolder.add(imagePath);
                                            uuidList.add(generateUUID());
                                            break;
                                        }
                                    }
                                }
                                else if(currentFormat.equals(allowedFormat) && (currentFormat.equals(PdfHandler.getPDFFORMAT()) == false))
                                {
                                    fileHolder.add(file);
                                    uuidList.add(generateUUID());
                                    break;
                                }
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
    }
}
