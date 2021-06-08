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
import ai.classifai.ui.launcher.conversion.ConverterLauncher;
import ai.classifai.ui.launcher.conversion.Task;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * PDF Handler
 *
 * @author codenamewei
 */
@Slf4j
@NoArgsConstructor
public class TifHandler
{
    public String saveTif2Image(@NonNull File tifFullPath, String outputPath, @NonNull String extensionFormat)
    {
        String message = null;
        String fileName = FileHandler.getFileName(tifFullPath.getAbsolutePath());

        try
        {
            List<File> tif2Images = new ArrayList<>();

            ImageInputStream is = ImageIO.createImageInputStream(tifFullPath);
            if (is == null || is.length() == 0)
            {
                return message;
            }

            Iterator<ImageReader> iterator = ImageIO.getImageReaders(is);
            if (iterator == null || !iterator.hasNext())
            {
                return message;
            }

            ImageReader reader = iterator.next();
            reader.setInput(is);

            int maxPages = reader.getNumImages(true);

            if (maxPages > ConverterLauncher.getMaxPage()) maxPages = ConverterLauncher.getMaxPage();

            for (int page = 0; page < maxPages; ++page)
            {
                if (Task.isStop()) break;

                String savedPath;
                if (outputPath == null)
                {
                    savedPath = FileHandler.getAbsolutePath(tifFullPath);
                }
                else
                {
                    savedPath = outputPath;
                }

                String imageSavedFullPath = savedPath + File.separator +  fileName + "_" + (page+1) + "." + extensionFormat;

                File fImageSavedFullPath = new File(imageSavedFullPath);

                if (fImageSavedFullPath.exists() == false)
                {
                    BufferedImage bim = reader.read(page);

                    if ((bim.getWidth() > ImageFileType.getMaxWidth()) || (bim.getHeight() > ImageFileType.getMaxHeight()))
                    {
                        log.debug("Image width and/or height bigger than " + ImageFileType.getMaxHeight());
                    }

                    // suffix in filename will be used as the file format
                    boolean bSavedSuccess = ImageIO.write(bim, extensionFormat, new File(imageSavedFullPath));

                    if (!bSavedSuccess)
                    {
                        String messageHeader = "Save TIF image failed: ";
                        message = messageHeader + tifFullPath.getName();
                        log.info(messageHeader + fImageSavedFullPath);
                        throw new Exception(messageHeader + fImageSavedFullPath);
                    }
                    else
                    {
                        tif2Images.add(fImageSavedFullPath);
                    }
                }
            }

        }
        catch (Exception e)
        {
            log.info("Tif Skipped. Failed in reading tif of file: " + tifFullPath, e);
        }
        return message;
    }
}
