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
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class TifHandler
{
    private static String[] TIFFORMAT = new String[]{"tif", "tiff"};
    private static final Integer MAX_ALLOWED_PAGES = 20; //only allow max 20 pages per document

    private static Integer dotsPerInch = 300; //standard dots per inch is 300

    public static boolean isTifFormat(String fileName)
    {
        for(String extension : TIFFORMAT)
        {
            Integer beginIndex = fileName.length() - extension.length();
            Integer endIndex = fileName.length();

            if(fileName.substring(beginIndex, endIndex).equals(extension))
            {
                return true;
            }
        }

        return false;
    }

    public static List<File> saveTif2Image(String fileName)
    {
        try {
            List<File> tif2Images = new ArrayList<>();


            ImageInputStream is = ImageIO.createImageInputStream(new File(fileName));
            if (is == null || is.length() == 0){
                return null;
            }

            Iterator<ImageReader> iterator = ImageIO.getImageReaders(is);
            if (iterator == null || !iterator.hasNext()) {
                return null;
            }

            ImageReader reader = (ImageReader) iterator.next();
            iterator = null;
            reader.setInput(is);

            int maxPages = reader.getNumImages(true);

            if(maxPages > MAX_ALLOWED_PAGES) maxPages = MAX_ALLOWED_PAGES;

            //FIX ME
            String pathFirstHalf = PdfHandler.getPathToFile(fileName);

            for (int page = 0; page < maxPages; ++page)
            {
                String imageSavedFullPath = pathFirstHalf + "_" + (page+1) + ".png";

                File fImageSavedFullPath = new File(imageSavedFullPath);

                if(fImageSavedFullPath.exists() == false)
                {
                    BufferedImage bim = reader.read(page);

                    if((bim.getWidth() > ImageFileType.getMaxWidth()) || (bim.getHeight() > ImageFileType.getMaxHeight()))
                    {

                        //TODO: is this the best way to handle this?

                        throw new Exception("Image width and/or height bigger than " + ImageFileType.getMaxHeight());
                    }


                    ImageIO.write(bim, "png", new File(imageSavedFullPath));

                    tif2Images.add(fImageSavedFullPath);

                }

            }

            return tif2Images;
        }
        catch(Exception e)
        {
            log.info("File Skipped. Failed to read in tif/tiff: " + fileName);
            log.debug("Error: ", e);
        }

        return null;
    }


}
