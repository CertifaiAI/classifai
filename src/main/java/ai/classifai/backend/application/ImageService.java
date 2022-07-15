package ai.classifai.backend.application;

import ai.classifai.backend.repository.database.DBUtils;
import ai.classifai.core.data.handler.ImageHandler;
import ai.classifai.core.dto.ProjectDTO;
import ai.classifai.core.entity.annotation.ImageEntity;
import ai.classifai.core.enumeration.AnnotationType;
import ai.classifai.core.loader.ProjectHandler;
import ai.classifai.core.loader.ProjectLoader;
import ai.classifai.core.loader.ProjectLoaderStatus;
import ai.classifai.core.properties.image.DataInfoProperties;
import ai.classifai.core.properties.image.ImageDTO;
import ai.classifai.core.service.annotation.ImageAnnotationService;
import ai.classifai.core.service.annotation.ImageDataRepository;
import ai.classifai.core.service.project.ProjectService;
import ai.classifai.core.status.FileSystemStatus;
import ai.classifai.core.utility.datetime.DateTime;
import ai.classifai.core.utility.parser.ProjectParser;
import ai.classifai.core.versioning.Version;
import ai.classifai.frontend.request.ThumbnailProperties;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@Slf4j
public class ImageService implements ImageAnnotationService<ImageDTO, ThumbnailProperties> {

    private final ImageDataRepository<ImageEntity, ImageDTO> imageDataRepository;
    private final ProjectService projectService;
    private final ProjectHandler projectHandler;

    public ImageService(ImageDataRepository<ImageEntity, ImageDTO> imageDataRepository,
                        ProjectService projectService,
                        ProjectHandler projectHandler) {
        this.imageDataRepository = imageDataRepository;
        this.projectService = projectService;
        this.projectHandler = projectHandler;
    }

    @Override
    public Future<ProjectLoader> createAnnotationProject(ProjectDTO projectDTO) {
        AnnotationType type = AnnotationType.get(projectDTO.getAnnotationType());
        ProjectLoader projectLoader = projectHandler.getProjectLoader(projectDTO.getProjectName(), type);

        return imageDataRepository.createAnnotationProject()
                .compose(res -> {
                    List<String> validImages = ImageHandler.getValidImagesFromFolder(new File(projectDTO.getProjectPath()));
                    List<Future> futures = new ArrayList<>();
                    boolean isLoadable = loadProjectRootPath(projectLoader, validImages);
                    int currentIndex = 1;
                    projectLoader.setFileSysTotalUUIDSize(validImages.size());

                    if (!isLoadable) {
                        try {
                            ImageHandler.getExampleImage(projectLoader.getProjectPath());
                        } catch (IOException e) {
                            log.debug("Loading files in project folder failed" + e);
                        }
                        return null;
                    }

                    for (String imagePath : validImages) {
                        try
                        {
                            ImageDTO imageDTO = ImageHandler.getAnnotation(new File(imagePath), projectDTO.getProjectId());
                            imageDTO.setAnnotationDict(ProjectParser.buildAnnotationDict(projectLoader));
                            futures.add(imageDataRepository.saveFilesMetaData(imageDTO));
                            projectLoader.getUuidAnnotationDict().put(imageDTO.getUuid(), imageDTO);
                            projectLoader.pushFileSysNewUUIDList(imageDTO.getUuid());
                            projectLoader.updateLoadingProgress(currentIndex);
                        }
                        catch (Exception e)
                        {
                            log.error("Fail to save metadata to database. " + e);
                        }
                        currentIndex++;
                    }


                    return Future.future(promise -> {
                        CompositeFuture.all(futures)
                                .onFailure(promise::fail)
                                .onSuccess(promise::complete);
                    });
                }).map(res -> projectLoader);
    }

    @Override
    public Future<ImageDTO> createAnnotation(ImageDTO boundingBoxDTO) throws Exception {
        return imageDataRepository.createAnnotation(boundingBoxDTO)
                .map(ImageEntity::toDto);
    }

    @Override
    public Future<List<ImageDTO>> listAnnotations(String projectName) {
        return imageDataRepository.listAnnotation(projectName)
                .map(res -> res.stream().map(ImageEntity::toDto).collect(Collectors.toList()));
    }

    @Override
    public Future<Optional<ImageDTO>> getAnnotationById(String projectName, String uuid) {
        return listAnnotations(projectName)
                .map(res -> res.stream()
                        .filter(dto -> dto.getUuid().equals(uuid))
                        .findFirst()
                );
    }

    @Override
    public Future<Void> updateAnnotation(@NonNull ImageDTO imageDTO, @NonNull ProjectLoader loader) {
        return imageDataRepository.updateAnnotation(imageDTO)
                .map(res -> {
                    Version version = loader.getProjectVersion().getCurrentVersion();
                    version.setLastModifiedDate(new DateTime());
                    projectService.updateLastModifiedDate(loader.getProjectId(), version.getDbFormat())
                            .onFailure(cause -> log.info("Databse update fail. Type: " + loader.getAnnotationType() + " Project: " + loader.getProjectName()));
                    return null;
                })
                .map(DBUtils::toVoid);
    }

    @Override
    public Future<Void> deleteData(@NonNull String projectName, @NonNull String uuid) {
        return imageDataRepository.deleteData(projectName, uuid)
                .map(DBUtils::toVoid);
    }

    @Override
    public Future<Void> deleteProjectById(@NonNull ProjectDTO projectDTO) {
        AnnotationType type = AnnotationType.get(projectDTO.getAnnotationType());
        ProjectLoader loader = projectHandler.getProjectLoader(projectDTO.getProjectName(), type);
        return imageDataRepository.deleteProjectById(loader.getProjectId())
                .map(DBUtils::toVoid);
    }

    @Override
    public Future<ProjectLoaderStatus> loadProject(ProjectLoader projectLoader) {
        Promise<ProjectLoaderStatus> promise = Promise.promise();

        imageDataRepository.loadAnnotationProject(projectLoader)
                .onComplete(res -> {
                    if (res.succeeded()) {
                        if (projectLoader.getIsProjectNew()) {
                            projectService.updateIsNewParam(projectLoader.getProjectId())
                                    .onFailure(promise::fail);
                        }

                        promise.complete(res.result());
                    }

                    if (res.failed()) {
                        promise.fail(res.cause());
                    }
                });

        return promise.future();
    }

    @Override
    public Future<String> renameData(@NonNull ProjectLoader loader, String uuid, String newFilename) {
        return imageDataRepository.renameData(loader, uuid, newFilename);
    }

    @Override
    public ImageDTO toDTO(ThumbnailProperties property, @NonNull ProjectLoader loader) {
        String uuid = property.getUuidParam();
        ImageDTO imageDTO = loader.getUuidAnnotationDict().get(uuid);

        imageDTO.setImgDepth(property.getImgDepth());
        imageDTO.setImgOriW(property.getImgOriWParam());
        imageDTO.setImgOriH(property.getImgOriHParam());
        imageDTO.setFileSize(property.getFileSizeParam());

        String currentVersionUuid = loader.getCurrentVersionUuid();

        DataInfoProperties version = imageDTO.getAnnotationDict().get(currentVersionUuid);

        if(loader.getAnnotationType().equals(AnnotationType.IMAGEBOUNDINGBOX.ordinal())) {
            version.setAnnotation(property.getBoundingBoxParam());
        } else if(loader.getAnnotationType().equals(AnnotationType.IMAGESEGMENTATION.ordinal())) {
            version.setAnnotation(property.getSegmentationParam());
        }

        version.setImgX(property.getImgXParam());
        version.setImgY(property.getImgYParam());
        version.setImgW(property.getImgWParam());
        version.setImgH(property.getImgHParam());

        return ImageDTO.builder()
                .projectId(loader.getProjectId())
                .uuid(property.getUuidParam())
                .imgOriW(property.getImgOriWParam())
                .imgOriH(property.getImgOriHParam())
                .imgPath(property.getImgPathParam())
                .imgDepth(property.getImgDepth())
                .annotationDict(imageDTO.getAnnotationDict())
                .build();
    }

    @Override
    public Future<ThumbnailProperties> getThumbnail(@NonNull ProjectLoader projectLoader, @NonNull String uuid) {
        return imageDataRepository.getThumbnail(projectLoader, uuid);
    }

    @Override
    public Future<String> getImageSource(@NonNull ProjectLoader projectLoader, @NonNull String uuid) {
        return imageDataRepository.getImageSource(projectLoader, uuid);
    }

    private boolean loadProjectRootPath(@NonNull ProjectLoader loader, @NonNull List<String> dataFullPathList)
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

        loader.setUnsupportedImageList(ImageHandler.getUnsupportedImagesFromFolder(rootPath));

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
//        if(!loader.getIsProjectNew())
//        {
//            for (int i = 0; i < dataFullPathList.size(); ++i)
//            {
//                imageDataRepository.createUuidIfNotExist(loader, new File(dataFullPathList.get(i)), i + 1);
//            }
//        }

        return true;
    }

    private void updateLastModifiedDate(ProjectLoader loader)
    {
        String projectID = loader.getProjectId();

        Version version = loader.getProjectVersion().getCurrentVersion();

        version.setLastModifiedDate(new DateTime());

        projectService.updateLastModifiedDate(projectID, version.getDbFormat())
                .onFailure(cause -> log.info("Databse update fail. Type: " + loader.getAnnotationType() + " Project: " + loader.getProjectName()));
    }

}
