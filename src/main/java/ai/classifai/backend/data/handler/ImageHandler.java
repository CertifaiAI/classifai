package ai.classifai.backend.data.handler;

import ai.classifai.backend.data.type.image.ImageData;
import ai.classifai.backend.data.type.image.ImageFileType;
import ai.classifai.backend.utility.FileHandler;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;

/**
 * Image Handler
 *
 * @author codenamewei
 */
@Slf4j
@NoArgsConstructor
public class ImageHandler {
    public static boolean isImageFileValid(File file)
    {
        try
        {
            ImageData imgData = ImageData.getImageData(file);

            if (imgData.getWidth() > ImageFileType.getMaxWidth() || imgData.getHeight() > ImageFileType.getMaxHeight())
            {
                log.info("Image size bigger than maximum allowed input size. Skipped " + file);
                return false;
            }
            else if (imgData.isAnimation()) {
                log.info("The image is animated and not supported ");
                return false;
            }
        }
        catch (Exception e)
        {
            log.debug(String.format("Skipped %s.%n%s", file, e.getMessage()));
            return false;
        }

        return true;
    }

    public static List<String> getValidImagesFromFolder(File rootPath)
    {
        return FileHandler.processFolder(rootPath, ImageHandler::isImageFileValid);
    }
}
