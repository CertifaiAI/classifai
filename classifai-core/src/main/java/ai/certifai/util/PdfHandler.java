package ai.certifai.util;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PdfHandler
{
    @Getter private static String PDFFORMAT = "pdf";

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

    public static List<File> savePdf2Image(String pdfFileName)
    {
        String[] subString = pdfFileName.split("/");
        String fullPathName = subString[subString.length - 1];

        try {
            PDDocument document = PDDocument.load(new File(pdfFileName));
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            List<File> pdf2Images = new ArrayList<>();

            String[] fileName = fullPathName.split("\\.");
            Integer pathLength = pdfFileName.length() - fullPathName.length();
            String pathToSave = pdfFileName.substring(0, pathLength);

            for (int page = 0; page < document.getNumberOfPages(); ++page)
            {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);

                //FIXME: NEED OPTIMIZATION
                String imageSavedFullPath = pathToSave + fileName[0] + (page+1) + ".png";

                File fImageSavedFullPath = new File(imageSavedFullPath);

                if(fImageSavedFullPath.exists() == false)
                {
                    pdf2Images.add(fImageSavedFullPath);

                    // suffix in filename will be used as the file format
                    ImageIOUtil.writeImage(bim, imageSavedFullPath, 300);
                }
            }

            document.close();

            return pdf2Images;
        }
        catch(Exception e)
        {
            log.error("PDF Skipped. Failed to read in pdf: " + fullPathName);
        }

        return null;
    }
}
