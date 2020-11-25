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
package ai.classifai.util.image;

import ai.classifai.data.type.image.ImageFileType;
import ai.classifai.ui.launcher.conversion.ConverterLauncher;
import ai.classifai.ui.launcher.conversion.Task;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import java.awt.image.BufferedImage;
import java.io.File;

/**
 * PDF Handler
 *
 * @author Chiawei Lim
 */
@Slf4j
@NoArgsConstructor
public class PdfHandler
{
    private final Integer DOTS_PER_INCH = 300;

    public String savePdf2Image(@NonNull File pdfFullPath, String outputPath, @NonNull String extensionFormat)
    {
        String message = null;

        PDDocument document = null;

        String fileName = FileHandler.getFileName(pdfFullPath.getAbsolutePath());
        try {
            document = PDDocument.load(pdfFullPath);
            PDFRenderer pdfRenderer = new PDFRenderer(document);

            int maxPages = document.getNumberOfPages();
            if(maxPages > ConverterLauncher.getMaxPage()) maxPages = ConverterLauncher.getMaxPage();

            for (int page = 0; page < maxPages; ++page)
            {
                if(Task.isStop()) break;

                String savedPath;

                if(outputPath == null)
                {
                    savedPath = FileHandler.getAbsolutePath(pdfFullPath);
                }
                else
                {
                    savedPath = outputPath;
                }

                String imageSavedFullPath = savedPath + File.separator +  fileName + "_" + (page+1) + "." + extensionFormat;

                File fImageSavedFullPath = new File(imageSavedFullPath);

                if(fImageSavedFullPath.exists() == false)
                {
                    BufferedImage bim = pdfRenderer.renderImageWithDPI(page, DOTS_PER_INCH, ImageType.RGB); //do it needs to be ImageType.COLOR or GRAY?

                    if((bim.getWidth() > ImageFileType.getMaxWidth()) || (bim.getHeight() > ImageFileType.getMaxHeight()))
                    {
                        document.close();
                        log.debug("Image width and/or height bigger than " + ImageFileType.getMaxHeight());
                    }

                    // suffix in filename will be used as the file format
                    ImageIOUtil.writeImage(bim, imageSavedFullPath, DOTS_PER_INCH);
                }
            }

            document.close();

        }
        catch(Exception e)
        {
            String messageHeader = "PDF Skipped. Failed in reading pdf of file: ";

            message = messageHeader + pdfFullPath.getName();

            log.info(messageHeader + pdfFullPath, e);
        }
        finally
        {
            try
            {
                if(document != null)
                {
                    document.close();
                }
            }
            catch(Exception e)
            {
                log.info("Error when closing pdf. ", e);
            }
        }

        return message;
    }
}
