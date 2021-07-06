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
        ImageData imageData = Objects.requireNonNull(ImageData.getImageData(filePath));

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

    //    private static String getImageHeader(String input)
//    {
//        Integer lastIndex = input.length();
//
//        Iterator<Map.Entry<String, String>> itr = ImageFileType.getBase64Header().entrySet().iterator();
//
//        while (itr.hasNext())
//        {
//            Map.Entry<String, String> entry = itr.next();
//
//            String fileFormat = input.substring(lastIndex - entry.getKey().length(), lastIndex);
//
//            if (fileFormat.equals(entry.getKey()))
//            {
//                return entry.getValue();
//            }
//        }
//
//        log.debug("File format not supported");
//
//        return null;
//    }
//
//    private static String base64FromBufferedImage(BufferedImage img)
//    {
//        try
//        {
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            ImageIO.write(img, "PNG", out);
//            byte[] bytes = out.toByteArray();
//            String base64bytes = Base64.getEncoder().encodeToString(bytes);
//            String src = "data:image/png;base64," + base64bytes;
//
//            return src;
//        }
//        catch (Exception e)
//        {
//            log.debug("Error in converting BufferedImage into base64: ", e);
//            return "";
//        }
//    }
//
//    public static boolean isImageReadable(File dataFullPath)
//    {
//        if ((dataFullPath.exists() == false) && (dataFullPath.length() < 5)) //length() stands for file size
//        {
//            log.debug(dataFullPath + " not found. Check if the data is in the corresponding path. ");
//
//            return false;
//        }
//
//        return true;
//    }
//
//    private static int getExifOrientation(File file)
//    {
//        try
//        {
//            Metadata metadata = JpegMetadataReader.readMetadata(file);
//            Directory dir= metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
//
//            return dir.getInt(274);
//        }
//        catch (Exception e)
//        {
//            return 0;
//        }
//    }
//
//    private static BufferedImage rotate(BufferedImage image, double angle)
//    {
//        double sin = Math.abs(Math.sin(angle));
//        double cos = Math.abs(Math.cos(angle));
//
//        int w = image.getWidth();
//        int h = image.getHeight();
//
//        int newW = (int) Math.floor(w * cos + h * sin);
//        int newH = (int) Math.floor(h * cos + w * sin);
//
//        int type = image.getType();
//        BufferedImage result = new BufferedImage(newW, newH, type);
//
//        Graphics2D g = result.createGraphics();
//
//        g.translate((newW - w) / 2, (newH - h) / 2);
//        g.rotate(angle,((double)w) / 2, ((double)h) / 2);
//        g.drawRenderedImage(image, null);
//
//        return result;
//    }
//
//    private static BufferedImage rotateWithOrientation(BufferedImage img, int orientation)
//    {
//        double angle = 0;
//
//        if (orientation == 8) angle = -Math.PI/2;
//        else if (orientation == 3) angle = Math.PI;
//        else if (orientation == 6) angle = Math.PI/2;
//
//        return rotate(img,angle);
//    }
//
//    private static int getHeight(BufferedImage img, int orientation)
//    {
//        if (orientation == 8 || orientation == 6)
//        {
//            return img.getWidth();
//        }
//
//        return img.getHeight();
//    }
//
//    private static int getWidth(BufferedImage img, int orientation)
//    {
//        if (orientation == 8 || orientation == 6)
//        {
//            return img.getHeight();
//        }
//        return img.getWidth();
//    }
//
//
//    public Map<String, String> getThumbNail(BufferedImage img, boolean toCheckOrientation, File file)
//    {
//        Map<String, String> imageData = new HashMap<>();
//
//        Integer oriHeight;
//        Integer oriWidth;
//
//        if(toCheckOrientation)
//        {
//            int orientation = getExifOrientation(file);
//
//            oriHeight = getHeight(img, orientation);
//            oriWidth = getWidth(img, orientation);
//
//            //rotate for thumbnail generation
//            img = rotateWithOrientation(img, orientation);
//        }
//        else
//        {
//            oriHeight = img.getHeight();
//            oriWidth = img.getWidth();
//        }
//
//        int type = img.getColorModel().getColorSpace().getType();
//        boolean grayscale = (type == ColorSpace.TYPE_GRAY || type == ColorSpace.CS_GRAY);
//
//        Integer depth = grayscale ? 1 : 3;
//
//        Integer thumbnailWidth = ImageFileType.getFixedThumbnailWidth();
//        Integer thumbnailHeight = ImageFileType.getFixedThumbnailHeight();
//
//        if (oriHeight > oriWidth)
//        {
//            thumbnailWidth =  thumbnailHeight * oriWidth / oriHeight;
//        }
//        else
//        {
//            thumbnailHeight = thumbnailWidth * oriHeight / oriWidth;
//        }
//
//        Image tmp = img.getScaledInstance(thumbnailWidth, thumbnailHeight, Image.SCALE_SMOOTH);
//        BufferedImage resized = new BufferedImage(thumbnailWidth, thumbnailHeight, BufferedImage.TYPE_INT_ARGB);
//        Graphics2D g2d = resized.createGraphics();
//        g2d.drawImage(tmp, 0, 0, null);
//        g2d.dispose();
//
//        imageData.put(ParamConfig.getImgDepth(), Integer.toString(depth));
//        imageData.put(ParamConfig.getImgOriHParam(), Integer.toString(oriHeight));
//        imageData.put(ParamConfig.getImgOriWParam(), Integer.toString(oriWidth));
//        imageData.put(ParamConfig.getBase64Param(), base64FromBufferedImage(resized));
//
//        return imageData;
//    }
//
//    public static String encodeFileToBase64Binary(File file)
//    {
//        try
//        {
//            FileInputStream fileInputStreamReader = new FileInputStream(file);
//
//            byte[] bytes = new byte[(int)file.length()];
//
//            fileInputStreamReader.read(bytes);
//
//            String encodedfile = new String(Base64.getEncoder().encode(bytes));
//
//            return getImageHeader(file.getAbsolutePath()) + encodedfile;
//        }
//        catch (Exception e)
//        {
//            log.error("Failed while converting File to base64", e);
//        }
//
//        return null;
//    }
//
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
        ImageData imgData = ImageData.getImageData(file.getAbsolutePath());

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
//
//    public static void saveToProjectTable(@NonNull ProjectLoader loader, List<String> filesPath)
//    {
//        loader.resetFileSysProgress(FileSystemStatus.DATABASE_UPDATING);
//        loader.setFileSysTotalUUIDSize(filesPath.size());
//
//        //cloud
//        if(loader.isCloud())
//        {
//            for (int i = 0; i < filesPath.size(); ++i)
//            {
//                AnnotationVerticle.saveDataPoint(loader, filesPath.get(i), i + 1);
//            }
//
//        }
//        //local file system
//        else
//        {
//            for (int i = 0; i < filesPath.size(); ++i)
//            {
//                String projectFullPath = loader.getProjectPath().getAbsolutePath();
//                String dataSubPath = StringHandler.removeFirstSlashes(FileHandler.trimPath(projectFullPath, filesPath.get(i)));
//
//                AnnotationVerticle.saveDataPoint(loader, dataSubPath, i + 1);
//            }
//
//        }
//    }
//
    public List<String> getValidImagesFromFolder(File rootPath)
    {
        return fileHandler.processFolder(rootPath, this::isImageFileValid);
    }
//
//    private static boolean isImageUnsupported(File file)
//    {
//        return (FileHandler.isFileSupported(file.getAbsolutePath(), ImageFileType.getImageFileTypes()) && !isImageFileValid(file));
//    }
//
//    private static List<String> getUnsupportedImagesFromFolder(File rootPath)
//    {
//        return FileHandler.processFolder(rootPath, ImageHandler::isImageUnsupported);
//    }
//
//
//    /**
//     * Iterate through project path to reflect changes
//     * when create/refresh project
//     *
//     * search through rootpath and check if list of files exists
//     *     scenario 1: root file missing
//     *     scenario 2: files missing - removed from ProjectLoader
//     *     scenario 3: existing uuids previously missing from current paths, but returns to the original paths
//     *     scenario 4: adding new files
//     *     scenario 5: evrything stills the same
//     */
//    public static boolean loadProjectRootPath(@NonNull ProjectLoader loader, boolean isNewProject)
//    {
//        if(isNewProject)
//        {
//            loader.resetFileSysProgress(FileSystemStatus.ITERATING_FOLDER);
//        }
//        else
//        {
//            //refreshing project
//            loader.resetReloadingProgress(FileSystemStatus.ITERATING_FOLDER);
//        }
//
//        File rootPath = loader.getProjectPath();
//
//        //scenario 1
//        if(!rootPath.exists())
//        {
//            loader.setSanityUuidList(new ArrayList<>());
//            loader.setFileSystemStatus(FileSystemStatus.ABORTED);
//
//            log.info("Project home path of " + rootPath.getAbsolutePath() + " is missing.");
//            return false;
//        }
//
//        List<String> dataFullPathList = getValidImagesFromFolder(rootPath);
//        loader.setUnsupportedImageList(getUnsupportedImagesFromFolder(rootPath));
//
//        //Scenario 2 - 1: root path exist but all images missing
//        if(dataFullPathList.isEmpty())
//        {
//            loader.getSanityUuidList().clear();
//            loader.setFileSystemStatus(FileSystemStatus.DATABASE_UPDATED);
//            return false;
//        }
//
//        loader.setFileSystemStatus(FileSystemStatus.DATABASE_UPDATING);
//
//        loader.setFileSysTotalUUIDSize(dataFullPathList.size());
//
//        //scenario 3 - 5
//        if(isNewProject)
//        {
//            saveToProjectTable(loader, dataFullPathList);
//        }
//        else // when refreshing project folder
//        {
//            for (int i = 0; i < dataFullPathList.size(); ++i)
//            {
//                AnnotationVerticle.createUuidIfNotExist(loader, new File(dataFullPathList.get(i)), i + 1);
//            }
//        }
//
//        return true;
//    }

}
