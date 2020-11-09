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
import ai.classifai.ui.launcher.conversion.ConverterLauncher;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.type.OS;
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
 * @author codenamewei
 */
@Slf4j
public class PdfHandler
{
    private final Integer DOTS_PER_INCH = 300; //standard dots per inch is 300
    private final String FILE_SEPARATOR;

    public PdfHandler()
    {
        if(ParamConfig.getOSManager().getCurrentOS().equals(OS.WINDOWS))
        {
            FILE_SEPARATOR = "\\\\";
        }
        else
        {
            FILE_SEPARATOR = File.separator;
        }
    }

    private String getFileName(@NonNull String pdfFilePath)
    {
        String[] subString = pdfFilePath.split(FILE_SEPARATOR);

        String fileNameWithExtension = subString[subString.length - 1];

        String[] separator = fileNameWithExtension.split("\\.");

        int fileEndIndex = pdfFilePath.length() -  separator[(separator.length - 1)].length() - 1;
        int fileStartIndex = pdfFilePath.length() - fileNameWithExtension.length();

        String fileName = pdfFilePath.substring(fileStartIndex, fileEndIndex);

        return fileName;
    }

    public String savePdf2Image(@NonNull File pdfFileName, @NonNull String outputPath, @NonNull String extensionFormat)
    {
        PDDocument document = null;

        String fileName = getFileName(pdfFileName.getAbsolutePath());

        try {
            document = PDDocument.load(pdfFileName);
            PDFRenderer pdfRenderer = new PDFRenderer(document);

            int maxPages = document.getNumberOfPages();
            if(maxPages > ConverterLauncher.getMaxPage()) maxPages = ConverterLauncher.getMaxPage();

            for (int page = 0; page < maxPages; ++page)
            {
                String imageSavedFullPath = outputPath + File.separator +  fileName + "_" + (page+1) + "." + extensionFormat;

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
            log.info("PDF Skipped. Failed in reading pdf of file: " + pdfFileName, e);
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
        return fileName + ".pdf";
    }
}
