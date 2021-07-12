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


import ai.classifai.data.type.image.ImageData;
import ai.classifai.data.type.image.ImageFileType;
import ai.classifai.data.type.image.JpegImageData;
import ai.classifai.database.model.Project;
import ai.classifai.database.model.data.Data;
import ai.classifai.database.model.data.Image;
import ai.classifai.util.Hash;
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
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Image Handler
 *
 * @author codenamewei
 */
@Slf4j
public class ImageHandler extends DataHandler
{
    public static final int THUMBNAIL_SIZE = 100;
    public static final int MAX_SIZE = 15000;

    @Override
    public List<Data> getDataList(Project project)
    {
        String projectPath = project.getProjectPath();
        List<String> imageFiles = getValidImagesFromFolder(new File(projectPath));

        return imageFiles.stream()
                .map(path -> fileToData(path, project))
                .collect(Collectors.toList());
    }

    @Override
    public String generateDataSource(Data data)
    {
        String fullPath = data.getFullPath();

        try
        {
            return getBase64String(fullPath);
        }
        catch (Exception ignored) {}

        log.error(String.format("Invalid path for %s", fullPath));
        return "";
    }

    // FIXME: rotateImage must fix
    public String generateThumbnail(Image image)
    {
        String fullPath = image.getFullPath();

        try
        {
            BufferedImage bufferedImage = ImageIO.read(new File(fullPath));

            int width = image.getWidth();
            int height = image.getHeight();

            // width, height
            Pair<Integer, Integer> thumbnailSize = getThumbnailSize(width, height);

            BufferedImage thumbnail = resizeImage(bufferedImage, thumbnailSize.getLeft(), thumbnailSize.getRight());

            return getBase64String(thumbnail);
        }
        catch (Exception ignored)
        {
            log.error(String.format("Invalid path for %s", fullPath));
            return "";
        }
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

    private Image fileToData(String filePath, Project project)
    {
        List<Data> existingData = project.getDataList();

        String projectPath = project.getProjectPath();
        File file = new File(filePath);
        ImageData imageData = Objects.requireNonNull(ImageData.getImageData(file));

        // general data
        // relative path
        String relativePath = fileHandler.trimPath(projectPath, filePath);
        // checksum
        String checksum = Hash.getHash256String(file);
        // file size
        long fileSize = file.length();

        // image data
        // image_depth
        int depth = imageData.getDepth();
        // image_width
        int width = imageData.getWidth();
        // image_height
        int height = imageData.getHeight();

        Image image = new Image(project, relativePath, checksum, fileSize, depth, width, height);

        int idx = existingData.indexOf(image);

        return idx == -1 ? image : (Image) existingData.get(idx);
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
        ImageData imageData = ImageData.getImageData(bytes);

        String base64bytes = Base64.getEncoder().encodeToString(bytes);
        String src = imageData.getBase64Header() + base64bytes;

        return src;
    }

    // width, height
    private Pair<Integer, Integer> getThumbnailSize(int width, int height)
    {
        int max = Math.max(width, height);
        int newWidth = THUMBNAIL_SIZE * width / max;
        int newHeight = THUMBNAIL_SIZE * height / max;

        return new ImmutablePair<>(newWidth, newHeight);
    }

    public boolean isImageFileValid(File file)
    {
        ImageData imgData = ImageData.getImageData(file);

        // is image valid
        if (imgData == null)
        {
            log.debug(String.format("Skipped %s.", file));
            return false;
        }

        // is image too big
        if (imgData.getWidth() > ImageFileType.getMaxWidth() || imgData.getHeight() > ImageFileType.getMaxHeight())
        {
            log.info("Image size bigger than maximum allowed input size. Skipped " + file);
            return false;
        }

        return true;
    }

    public List<String> getValidImagesFromFolder(File rootPath)
    {
        return fileHandler.processFolder(rootPath, this::isImageFileValid);
    }
}
