package ai.classifai.service;

import ai.classifai.core.entity.dto.image.ImageDataDTO;
import ai.classifai.core.entity.model.image.ImageData;
import ai.classifai.data.image.ImageMetadata;
import ai.classifai.util.Hash;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class ImageService extends DataService
{
    public final int THUMBNAIL_SIZE = 100;
    public final int MAX_SIZE = 15000;

    public ImageService(Vertx vertx)
    {
        super(vertx);
    }

    public Future<List<ImageDataDTO>> getImageDTOList(String path)
    {
        return vertx.executeBlocking(promise ->
        {
            List<File> fileList = listFileRecursively(new File(path))
                    .stream()
                    .filter(this::isImageFileValid)
                    .collect(Collectors.toList());

            List<ImageMetadata> imageMetadataList = fileList.stream()
                    .map(ImageMetadata::getImageMetadata)
                    .collect(Collectors.toList());

            promise.complete(IntStream.range(0, imageMetadataList.size())
                    .mapToObj(idx -> getImageDTO(fileList.get(idx), imageMetadataList.get(idx)))
                    .collect(Collectors.toList()));
        });
    }

    private ImageDataDTO getImageDTO(File file, ImageMetadata imageMetadata)
    {
        return ImageDataDTO.builder()
                .path(file.getAbsolutePath())
                .checksum(Hash.getHash256String(file))
                .fileSize(file.length())
                .depth(imageMetadata.getDepth())
                .width(imageMetadata.getWidth())
                .height(imageMetadata.getHeight())
                .build();
    }

    public Future<String> getThumbnail(ImageData image)
    {
        return vertx.executeBlocking(promise ->
        {
            String dataPath = image.getPath();

            try
            {
                BufferedImage bufferedImage = ImageIO.read(new File(dataPath));

                int width = image.getWidth();
                int height = image.getHeight();

                // width, height
                Pair<Integer, Integer> thumbnailSize = getThumbnailSize(width, height);

                BufferedImage thumbnail = resizeImage(bufferedImage, thumbnailSize.getLeft(), thumbnailSize.getRight());

                promise.complete(getBase64String(thumbnail));
            }
            catch (Exception e)
            {
                String errorMsg = String.format("Invalid path for %s", dataPath);
                log.error(errorMsg);
                promise.fail(errorMsg);
            }
        });
    }

    public Future<String> getImageSource(ImageData image)
    {
        return vertx.executeBlocking(promise ->
        {
            String dataPath = image.getPath();

            try
            {
                promise.complete(getBase64String(dataPath));
            }
            catch (Exception e)
            {
                String errorMsg = String.format("Invalid path for %s", dataPath);
                log.error(errorMsg);
                promise.fail(errorMsg);
            }
        });
    }

    public String generateDataSource(ImageData image)
    {
        String dataPath = image.getPath();

        try
        {
            return getBase64String(dataPath);
        }
        catch (Exception ignored) {}

        log.error(String.format("Invalid path for %s", dataPath));
        return "";
    }

    public Boolean isImageFileValid(File file)
    {
        ImageMetadata imgData = ImageMetadata.getImageMetadata(file);

        // is image valid
        if (imgData == null)
        {
            log.debug(String.format("Skipped %s.", file));
            return false;
        }

        // is image too big
        if (imgData.getWidth() > MAX_SIZE || imgData.getHeight() > MAX_SIZE)
        {
            log.info("Image size bigger than maximum allowed input size. Skipped " + file);
            return false;
        }

        return true;
    }

    private String getBase64String(BufferedImage img)
    {
        try
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(img, "PNG", out);
            byte[] bytes = out.toByteArray();

            return getBase64String(bytes);
        }
        catch (Exception e)
        {
            log.debug("Error in converting BufferedImage into base64: ", e);
            return "";
        }
    }

    private String getBase64String(String filePath)
    {
        try
        {
            File file = new File(filePath);
            byte[] bytes = FileUtils.readFileToByteArray(file);

            return getBase64String(bytes);
        } catch (Exception e)
        {
            log.debug("Error in converting BufferedImage into base64: ", e);
            return "";
        }
    }

    private String getBase64String(byte[] bytes)
    {
        ImageMetadata imageMetadata = ImageMetadata.getImageMetadata(bytes);

        String base64bytes = Base64.getEncoder().encodeToString(bytes);

        return imageMetadata.getBase64Header() + base64bytes;
    }

    // width, height
    private Pair<Integer, Integer> getThumbnailSize(int width, int height)
    {
        int max = Math.max(width, height);
        int newWidth = THUMBNAIL_SIZE * width / max;
        int newHeight = THUMBNAIL_SIZE * height / max;

        return new ImmutablePair<>(newWidth, newHeight);
    }

    private BufferedImage resizeImage(BufferedImage bufferedImage, Integer width, Integer height)
    {
        java.awt.Image img = bufferedImage.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();

        return resized;
    }
}
