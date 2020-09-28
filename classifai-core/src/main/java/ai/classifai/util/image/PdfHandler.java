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
package ai.classifai.util.image;

import ai.classifai.data.type.image.ImageFileType;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * PDF Handler
 *
 * @author Chiawei Lim
 */
@Slf4j
public class PdfHandler
{
    private static String PDFFORMAT = "pdf";
    private static final Integer MAX_ALLOWED_PAGES = 20; //only allow max 20 pages per document
    private static Integer dotsPerInch = 300; //standard dots per inch is 300

    public static boolean isPdf(String pdfFileName)
    {
        Integer beginIndex = pdfFileName.length() - PDFFORMAT.length();
        Integer endIndex = pdfFileName.length();

        if(pdfFileName.substring(beginIndex, endIndex).equals(PDFFORMAT))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    //FIXME: Poorly written
    public static String getPathToFile(String pdfFileName)
    {
        String[] subString = pdfFileName.split("/");
        String fullPathName = subString[subString.length - 1];

        String[] separator = fullPathName.split("\\.");

        int fileEndIndex = fullPathName.length() -  separator[(separator.length - 1)].length() - 1;
        String fileName = fullPathName.substring(0, fileEndIndex);

        Integer pathLength = pdfFileName.length() - fullPathName.length();
        String pathToSave = pdfFileName.substring(0, pathLength);

        fileName = fileName.replace(".", "_"); //replace any possible "." with "_"
        fileName = fileName.replace(" ", ""); //replace any possible " " with ""

        String pathFirstHalf = pathToSave + fileName;

        return pathFirstHalf;
    }

    public static List<File> savePdf2Image(String pdfFileName)
    {
        try {
            PDDocument document = PDDocument.load(new File(pdfFileName));
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            List<File> pdf2Images = new ArrayList<>();

            int maxPages = document.getNumberOfPages();
            if(maxPages > MAX_ALLOWED_PAGES) maxPages = MAX_ALLOWED_PAGES;

            String pathFirstHalf = getPathToFile(pdfFileName);

            for (int page = 0; page < maxPages; ++page)
            {
                String imageSavedFullPath = pathFirstHalf + "_" + (page+1) + ".png";

                File fImageSavedFullPath = new File(imageSavedFullPath);

                if(fImageSavedFullPath.exists() == false)
                {
                    BufferedImage bim = pdfRenderer.renderImageWithDPI(page, dotsPerInch, ImageType.RGB); //do it needs to be ImageType.COLOR or GRAY?

                    if((bim.getWidth() > ImageFileType.getMaxWidth()) || (bim.getHeight() > ImageFileType.getMaxHeight()))
                    {
                        document.close();
                        log.debug("Image width and/or height bigger than " + ImageFileType.getMaxHeight());
                    }

                    pdf2Images.add(fImageSavedFullPath);

                    // suffix in filename will be used as the file format
                    ImageIOUtil.writeImage(bim, imageSavedFullPath, dotsPerInch);
                }
            }

            document.close();

            return pdf2Images;
        }
        catch(Exception e)
        {
            log.info("PDF Skipped. Failed in reading pdf of file: " + pdfFileName, e);
        }

        return null;
    }
}
