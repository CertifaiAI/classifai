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
import ai.classifai.database.annotation.AnnotationDB;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.ui.enums.FileSystemStatus;
import ai.classifai.util.ParamConfig;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Image Handler
 *
 * @author codenamewei
 */
@Slf4j
public class ImageHandler {
    @Setter @Getter private static int currentAddedImages;
    @Setter @Getter private static int totalImagesToBeAdded;
    @Setter @Getter private static int currentAddedFolders;
    @Setter @Getter private static int totalFoldersToBeAdded;

    public static BufferedImage toBufferedImage(Mat matrix)
    {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (matrix.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = matrix.channels() * matrix.cols() * matrix.rows();
        byte[] buffer = new byte[bufferSize];
        matrix.get(0, 0, buffer); // get all the pixels
        BufferedImage image = new BufferedImage(matrix.cols(), matrix.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);
        return image;
    }

    private static String getImageHeader(String input)
    {
        Integer lastIndex = input.length();

        Iterator<Map.Entry<String, String>> itr = ImageFileType.getBase64Header().entrySet().iterator();

        while (itr.hasNext())
        {
            Map.Entry<String, String> entry = itr.next();

            String fileFormat = input.substring(lastIndex - entry.getKey().length(), lastIndex);

            if (fileFormat.equals(entry.getKey()))
            {
                return entry.getValue();
            }
        }

        log.debug("File format not supported");

        return null;
    }

    private static String base64FromBufferedImage(BufferedImage img)
    {
        try
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(img, "PNG", out);
            byte[] bytes = out.toByteArray();
            String base64bytes = Base64.getEncoder().encodeToString(bytes);
            String src = "data:image/png;base64," + base64bytes;

            return src;
        }
        catch (Exception e)
        {
            log.debug("Error in converting BufferedImage into base64: ", e);
            return "";
        }
    }

    public static boolean isImageReadable(File dataFullPath)
    {
        if ((dataFullPath.exists() == false) && (dataFullPath.length() < 5)) //length() stands for file size
        {
            log.debug(dataFullPath + " not found. Check if the data is in the corresponding path. ");

            return false;
        }

        return true;
    }

    private static BufferedImage rotateWithOrientation(BufferedImage image) throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        ImageData imgData = ImageData.getImageData(out.toByteArray());

        double angle = imgData.getAngle();

        double sin = Math.abs(Math.sin(angle));
        double cos = Math.abs(Math.cos(angle));

        int w = imgData.getWidth();
        int h = imgData.getHeight();

        int newW = (int) Math.floor(w * cos + h * sin);
        int newH = (int) Math.floor(h * cos + w * sin);
        int type = image.getType();

        BufferedImage result = new BufferedImage(newW, newH, type);

        Graphics2D g = result.createGraphics();

        g.translate((newW - w) / 2, (newH - h) / 2);
        g.rotate(angle,(double) w / 2, (double) h / 2);
        g.drawRenderedImage(image, null);

        return result;
    }

    public static Map<String, String> getThumbNail(BufferedImage image) throws IOException {
        Map<String, String> imageData = new HashMap<>();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        ImageData imgData = ImageData.getImageData(out.toByteArray());

        int oriWidth = imgData.getWidth();

        int oriHeight = imgData.getHeight();

        int depth = imgData.getDepth();

        image = rotateWithOrientation(image);

        Integer thumbnailWidth = ImageFileType.getFixedThumbnailWidth();
        Integer thumbnailHeight = ImageFileType.getFixedThumbnailHeight();

        if (oriHeight > oriWidth)
        {
            thumbnailWidth =  thumbnailHeight * oriWidth / oriHeight;
        }
        else
        {
            thumbnailHeight = thumbnailWidth * oriHeight / oriWidth;
        }

        Image tmp = image.getScaledInstance(thumbnailWidth, thumbnailHeight, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(thumbnailWidth, thumbnailHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        imageData.put(ParamConfig.getImgDepth(), Integer.toString(depth));
        imageData.put(ParamConfig.getImgOriHParam(), Integer.toString(imgData.getHeight()));
        imageData.put(ParamConfig.getImgOriWParam(), Integer.toString(imgData.getWidth()));
        imageData.put(ParamConfig.getBase64Param(), base64FromBufferedImage(resized));

        return imageData;
    }

    public static String encodeFileToBase64Binary(File file)
    {
        try
        {
            FileInputStream fileInputStreamReader = new FileInputStream(file);

            byte[] bytes = new byte[(int)file.length()];

            fileInputStreamReader.read(bytes);

            String encodedfile = new String(Base64.getEncoder().encode(bytes));

            fileInputStreamReader.close();

            return getImageHeader(file.getAbsolutePath()) + encodedfile;
        }
        catch (Exception e)
        {
            log.error("Failed while converting File to base64", e);
        }

        return null;
    }

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

    public static void saveToProjectTable(@NonNull AnnotationDB annotationDB, @NonNull ProjectLoader loader, List<String> filesPath)
    {
        loader.resetFileSysProgress(FileSystemStatus.DATABASE_UPDATING);
        loader.setFileSysTotalUUIDSize(filesPath.size());

        //cloud
        if(loader.isCloud())
        {
            for (int i = 0; i < filesPath.size(); ++i)
            {
                annotationDB.saveDataPoint(loader, filesPath.get(i), i + 1);
            }

        }
        //local file system
        else
        {
            for (int i = 0; i < filesPath.size(); ++i)
            {
                String projectFullPath = loader.getProjectPath().getAbsolutePath();
                String dataSubPath = StringHandler.removeFirstSlashes(FileHandler.trimPath(projectFullPath, filesPath.get(i)));

                annotationDB.saveDataPoint(loader, dataSubPath, i + 1);
            }

        }
    }

    public static List<String> getValidImagesFromFolder(File rootPath)
    {
        return FileHandler.processFolder(rootPath, ImageHandler::isImageFileValid);
    }

    private static boolean isImageUnsupported(File file)
    {
        return (FileHandler.isFileSupported(file.getAbsolutePath(), ImageFileType.getImageFileTypes()) && !isImageFileValid(file));
    }

    private static List<String> getUnsupportedImagesFromFolder(File rootPath)
    {
        return FileHandler.processFolder(rootPath, ImageHandler::isImageUnsupported);
    }


    /**
     * Iterate through project path to reflect changes
     * when create/refresh project
     *
     * search through rootpath and check if list of files exists
     *     scenario 1: root file missing
     *     scenario 2: files missing - removed from ProjectLoader
     *     scenario 3: existing uuids previously missing from current paths, but returns to the original paths
     *     scenario 4: adding new files
     *     scenario 5: evrything stills the same
     */
    public static boolean loadProjectRootPath(@NonNull ProjectLoader loader, @NonNull AnnotationDB annotationDB)
    {
        if(loader.getIsProjectNew())
        {
            loader.resetFileSysProgress(FileSystemStatus.ITERATING_FOLDER);
        }
        else
        {
            //refreshing project
            loader.resetReloadingProgress(FileSystemStatus.ITERATING_FOLDER);
        }

        File rootPath = loader.getProjectPath();

        //scenario 1
        if(!rootPath.exists())
        {
            loader.setSanityUuidList(new ArrayList<>());
            loader.setFileSystemStatus(FileSystemStatus.ABORTED);

            log.info("Project home path of " + rootPath.getAbsolutePath() + " is missing.");
            return false;
        }

        List<String> dataFullPathList = getValidImagesFromFolder(rootPath);
        loader.setUnsupportedImageList(getUnsupportedImagesFromFolder(rootPath));

        //Scenario 2 - 1: root path exist but all images missing
        if(dataFullPathList.isEmpty())
        {
            loader.getSanityUuidList().clear();
            loader.setFileSystemStatus(FileSystemStatus.DATABASE_UPDATED);
            return false;
        }

        loader.setFileSystemStatus(FileSystemStatus.DATABASE_UPDATING);

        loader.setFileSysTotalUUIDSize(dataFullPathList.size());

        //scenario 3 - 5
        if(loader.getIsProjectNew())
        {
            saveToProjectTable(annotationDB, loader, dataFullPathList);
        }
        else // when refreshing project folder
        {
            for (int i = 0; i < dataFullPathList.size(); ++i)
            {
                annotationDB.createUuidIfNotExist(loader, new File(dataFullPathList.get(i)), i + 1);
            }
        }

        return true;
    }

    public static void createBackUpFolder(String backUpFolderPath, List<String> addedFileList, List<String> currentFolderList, List<String> fileNames,
                                          File projectPath, Integer index, Boolean file) throws IOException
    {
        File backUpFolder = new File(backUpFolderPath);

        if(!backUpFolder.exists()){
            backUpFolder.mkdir();
            log.info("Image Backup folder for storing similar name image file is created at " + projectPath.getParent());
        }

        int fileIndex = fileNames.indexOf(FilenameUtils.getName(addedFileList.get(index)));

        if(Boolean.TRUE.equals(file))
        {
            FileUtils.moveFileToDirectory(new File(currentFolderList.get(fileIndex)), backUpFolder, false);
        }
        else
        {
            FileUtils.moveDirectoryToDirectory(new File(currentFolderList.get(fileIndex)), backUpFolder, false);
        }
    }

    public static void addImageToProjectFolder(List<String> imageNameList, List<String> imageBase64List, File projectPath,
                                               List<String> currentFolderFiles)
    {
        totalImagesToBeAdded = imageNameList.size();

        for(int i = 0; i < imageNameList.size(); i++)
        {
            try
            {
                //decode image base64 string into image
                byte[] decodedBytes = Base64.getDecoder().decode(imageBase64List.get(i).split("base64,")[1]);
                File imageFile = new File(projectPath.getAbsolutePath() + File.separator + imageNameList.get(i));
                currentAddedImages = i + 1; //to make count start at 1

                if(!currentFolderFiles.contains(imageFile.getAbsolutePath()))
                {
                    FileUtils.writeByteArrayToFile(imageFile, decodedBytes);
                    log.info(imageFile.getName() + " is added to project folder " + projectPath.getName());
                }
                else
                {
                    log.info(imageFile.getName() + " is exist in current folder");
                    log.info("Operation add " + imageFile.getName() + " to project folder " + projectPath.getName() + " aborted");
                }
            }
            catch (IOException e)
            {
                log.info("Fail to convert Base64 String to Image file");
                return;
            }
        }
    }

    public static void moveImageToProjectFolder(List<String> imageFilePathList, Boolean modifyImageOrFolderName, Boolean replaceImageOrFolder,
                                                String backUpFolderPath, File projectPath) throws IOException
    {
        List<String> currentFolderFileNames = FileHandler.processFolder(projectPath, ImageHandler::isImageFileValid);
        List<String> fileNames = currentFolderFileNames.stream().map(FilenameUtils::getName).collect(Collectors.toList());
        totalImagesToBeAdded = imageFilePathList.size();

        for (int i = 0; i < imageFilePathList.size(); i++)
        {
            currentAddedImages = i + 1;
            try
            {
                if(fileNames.contains(FilenameUtils.getName(imageFilePathList.get(i))))
                {
                    createBackUpFolder(backUpFolderPath, imageFilePathList, currentFolderFileNames,
                            fileNames, projectPath, i, true);
                }

                if(Boolean.TRUE.equals(!modifyImageOrFolderName) && Boolean.TRUE.equals(replaceImageOrFolder))
                {
                    String fileName = FilenameUtils.getName(imageFilePathList.get(i));
                    File deleteFile = new File(backUpFolderPath + fileName);
                    Files.delete(deleteFile.toPath());
                    FileUtils.moveFileToDirectory(new File(imageFilePathList.get(i)), projectPath, false);
                    log.info("Original image was replaced by selected image");
                }

                if(Boolean.TRUE.equals(modifyImageOrFolderName) && Boolean.TRUE.equals(!replaceImageOrFolder))
                {
                    FileUtils.moveFileToDirectory(new File(imageFilePathList.get(i)), projectPath, false);
                    String fileName = FilenameUtils.getName(imageFilePathList.get(i));
                    String fileBaseName = FilenameUtils.getBaseName(imageFilePathList.get(i));
                    String fileExtension = FilenameUtils.getExtension(imageFilePathList.get(i));
                    File oldFile = new File(projectPath.getPath() + File.separator + fileName);
                    File newFile = new File(projectPath.getAbsolutePath() + File.separator
                            + fileBaseName + "_" + i + "." + fileExtension);
                    Files.move(newFile.toPath(), oldFile.toPath());
                    log.info("Selected image has been renamed to " + newFile.getName());
                }

                if(Boolean.TRUE.equals(!modifyImageOrFolderName) && Boolean.TRUE.equals(!replaceImageOrFolder))
                {
                    FileUtils.moveFileToDirectory(new File(imageFilePathList.get(i)), projectPath, false);
                    log.info("Selected image has been moved to current project folder");
                }
            }
            catch (IOException e)
            {
                log.info("Fail to move selected images to current project folder");
            }
        }
    }

    public static void moveImageFolderToProjectFolder(List<String> imageDirectoryList, Boolean modifyFolderName, Boolean replaceFolder,
                                                      String backUpFolderPath, File projectPath) throws IOException
    {
        File[] filesList = projectPath.listFiles();

        List<String> folderList = Arrays.stream(Objects.requireNonNull(filesList))
                .map(File::getAbsolutePath)
                .collect(Collectors.toList());

        List<String> folderNames = Arrays.stream(filesList)
                .map(Objects::requireNonNull)
                .filter(File::isDirectory)
                .map(File::getName)
                .collect(Collectors.toList());

        totalFoldersToBeAdded = imageDirectoryList.size();

        for (int j = 0; j < imageDirectoryList.size(); j++)
        {
            currentAddedFolders = j + 1;
            try
            {
                if (folderNames.contains(FilenameUtils.getName(imageDirectoryList.get(j))))
                {
                    createBackUpFolder(backUpFolderPath, imageDirectoryList, folderList,
                            folderNames, projectPath, j, false);
                }

                if (Boolean.TRUE.equals(!modifyFolderName) && Boolean.TRUE.equals(replaceFolder))
                {
                    String folderName = FilenameUtils.getName(imageDirectoryList.get(j));
                    File deleteFolder = new File(backUpFolderPath + folderName);
                    FileUtils.deleteDirectory(deleteFolder);
                    FileUtils.moveDirectoryToDirectory(new File(imageDirectoryList.get(j)), projectPath, false);
                    log.info("The original folder was replaced by selected folder");
                }

                if (Boolean.TRUE.equals(modifyFolderName) && Boolean.TRUE.equals(!replaceFolder))
                {
                    FileUtils.moveDirectoryToDirectory(new File(imageDirectoryList.get(j)), projectPath, false);
                    String folderBaseName = FilenameUtils.getBaseName(imageDirectoryList.get(j));
                    String folderModifyName = folderBaseName + "_" + j;
                    File oldFolder = new File(projectPath.getPath() + File.separator + folderBaseName);
                    File newFolder = new File(projectPath.getAbsolutePath() + File.separator + folderModifyName);
                    Files.move(oldFolder.toPath(), newFolder.toPath());
                    log.info("Selected folder has been renamed to " + newFolder.getName());
                }

                if (Boolean.TRUE.equals(!modifyFolderName) && Boolean.TRUE.equals(!replaceFolder))
                {
                    FileUtils.moveDirectoryToDirectory(new File(imageDirectoryList.get(j)), projectPath, false);
                    log.info("Selected folder has moved into current project folder");
                }
            }
            catch (IOException e)
            {
                log.info("Fail to move selected image folder to current project folder");
            }
        }
    }

}
