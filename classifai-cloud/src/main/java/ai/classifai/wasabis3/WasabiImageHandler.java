package ai.classifai.wasabis3;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;


/**
 * Wasabi image handler
 *
 * @author codenamewei
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WasabiImageHandler
{
    public static String encodeFileToBase64Binary(@NonNull WasabiProject wasabiProject, @NonNull String dataPath)
    {
        byte[] bytes = getObject(wasabiProject, dataPath);

        return Base64.getEncoder().encodeToString(bytes);
    }

    public static BufferedImage getThumbNail(@NonNull WasabiProject wasabiProject, @NonNull String dataPath)
    {
        byte[] bytes = getObject(wasabiProject, dataPath);

        return bytesToBufferedImage(bytes);
    }

    private static byte[] getObject(@NonNull WasabiProject project, @NonNull String key)
    {
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(project.getWasabiBucket())
                .key(key)
                .range("*")
                .build();

        ResponseInputStream<GetObjectResponse> objectPortion = project.getWasabiS3Client().getObject(objectRequest);

        if(objectPortion != null)
        {
            try
            {
                byte[] bytes = objectPortion.readAllBytes();

                return bytes;
            }
            catch(Exception e)
            {
                log.info("Read data wrong");
            }
        }
        else
        {
            log.info("Object response is null: " + project.getWasabiBucket() + " " + key);
        }

        return null;
    }

    private static BufferedImage bytesToBufferedImage(byte[] imageData)
    {
        ByteArrayInputStream byteArray = new ByteArrayInputStream(imageData);

        try
        {
            return ImageIO.read(byteArray);
        }
        catch (IOException e)
        {
            log.debug("Failure in the conversion from bytes to BufferedImage");
        }

        return null;
    }


}
