package ai.classifai.backend.repository.service;

import ai.classifai.backend.repository.JDBCPoolHolder;
import ai.classifai.backend.repository.database.DBUtils;
import ai.classifai.backend.repository.query.AnnotationQuery;
import ai.classifai.backend.repository.query.PortfolioDbQuery;
import ai.classifai.backend.repository.query.QueryOps;
import ai.classifai.backend.utility.action.rename.RenameDataErrorCode;
import ai.classifai.backend.utility.action.rename.RenameProjectData;
import ai.classifai.core.data.handler.ImageHandler;
import ai.classifai.core.entity.annotation.ImageEntity;
import ai.classifai.core.enumeration.AnnotationType;
import ai.classifai.core.loader.ProjectLoader;
import ai.classifai.core.loader.ProjectLoaderStatus;
import ai.classifai.core.properties.image.DataInfoProperties;
import ai.classifai.core.properties.image.ImageDTO;
import ai.classifai.core.service.annotation.ImageDataRepository;
import ai.classifai.core.utility.ParamConfig;
import ai.classifai.core.utility.parser.ProjectParser;
import ai.classifai.frontend.request.ThumbnailProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
public class ImageRepoService implements ImageDataRepository<ImageEntity, ImageDTO> {
    private final JDBCPool annotationPool;
    private final QueryOps queryOps = new QueryOps();

    public ImageRepoService(JDBCPoolHolder jdbcHolder) {
        this.annotationPool = jdbcHolder.getAnnotationPool();
    }

    private ImageEntity toEntity(ImageDTO imageDTO) {
        return ImageEntity.builder()
                .uuid(imageDTO.getUuid())
                .imgDepth(imageDTO.getImgDepth())
                .imgOriW(imageDTO.getImgOriW())
                .imgOriH(imageDTO.getImgOriH())
                .fileSize(imageDTO.getFileSize())
                .build();
    }

    @Override
    public Future<Void> createAnnotationProject() {
        return queryOps.runQuery(AnnotationQuery.getCreateImageProject(), annotationPool)
                .map(DBUtils::toVoid);
    }

    @Override
    public Future<Void> saveFilesMetaData(ImageDTO imageDTO) {
        Tuple params = Tuple.of(
                imageDTO.getUuid(),
                imageDTO.getProjectId(),
                imageDTO.getImgPath(),
                imageDTO.getAnnotationDictDbFormat(),
                imageDTO.getImgDepth(),
                imageDTO.getImgOriW(),
                imageDTO.getImgOriH(),
                imageDTO.getFileSize()
        );

        return queryOps.runQuery(AnnotationQuery.getCreateImageData(), params, annotationPool)
                .map(DBUtils::toVoid);
    }

    @Override
    public Future<Void> deleteData(@NonNull String projectName, @NonNull String uuid) {
        Promise<Void> promise = Promise.promise();
//        ProjectLoader loader = Objects.requireNonNull(projectHandler.getProjectLoader(projectId));
//        String uuidQueryParam = String.join(",", deleteUUIDList);
//
//        Tuple params = Tuple.of(projectId, uuidQueryParam);
//
//        return queryOps.runQuery(AnnotationQuery.getDeleteImageProjectData(), params, annotationPool)
//                .map(result -> {
//                    try {
//                        return deleteProjectDataOnComplete(loader, deleteUUIDList, uuidImgPathList);
//                    } catch (IOException e) {
//                        log.info("Fail to delete. IO exception occurs.");
//                    }
//                    return loader.getSanityUuidList();
//                });
        return promise.future();
    }

    @Override
    public Future<ImageEntity> createAnnotation(@NonNull ImageDTO imageDTO) throws JsonProcessingException {
        ImageEntity entity = toEntity(imageDTO);
        String boundingBoxPropertiesString = writeJsonString(entity);
        Tuple params = Tuple.of(boundingBoxPropertiesString);
        return queryOps.runQuery(AnnotationQuery.getUpdateImageData(), params, annotationPool)
                .map(res -> entity);
    }

    @Override
    public Future<List<ImageEntity>> listAnnotation(@NonNull String projectName) {
        Tuple param = Tuple.of(projectName);
        return queryOps.runQuery(AnnotationQuery.getRetrieveAllImageProjects(), param, annotationPool)
                .map(res -> {
                    if (res.size() != 0) {
                        List<ImageEntity> list = new ArrayList<>();
                        for (Row row : res.value()) {
                            ImageEntity entity = ImageEntity.builder()
                                    .build();
                            list.add(entity);
                        }
                        return list;
                    }
                    log.info("Failed to retrieve annotation data for project " + projectName);
                    return null;
                });
    }

    @Override
    public Future<Void> updateAnnotation(@NonNull ImageDTO imageDTO) {
        Tuple params = imageDTO.getTuple();
        return queryOps.runQuery(AnnotationQuery.getUpdateImageData(), params, annotationPool)
                .map(DBUtils::toVoid);
    }

    @Override
    public Future<Void> deleteProjectById(@NonNull String projectId) {
        Tuple params = Tuple.of(projectId);
        return queryOps.runQuery(AnnotationQuery.getDeleteImageProject(), params, annotationPool)
                .map(DBUtils::toVoid);
    }

    @Override
    public Future<ProjectLoaderStatus> loadAnnotationProject(@NonNull ProjectLoader projectLoader) {
        Promise<ProjectLoaderStatus> promise = Promise.promise();

        projectLoader.toggleFrontEndLoaderParam();

        loadProject(projectLoader).onComplete(res -> {
            if (res.succeeded()) {
                ProjectLoaderStatus projectLoaderStatus = projectLoader.getProjectLoaderStatus();

                if(projectLoaderStatus.equals(ProjectLoaderStatus.DID_NOT_INITIATED))
                {
                    projectLoader.setProjectLoaderStatus(ProjectLoaderStatus.LOADING);
                    promise.complete(ProjectLoaderStatus.LOADING);
                }
                else if (projectLoaderStatus.equals(ProjectLoaderStatus.LOADED)) {
                    promise.complete(ProjectLoaderStatus.LOADED);
                }
                else if(projectLoaderStatus.equals(ProjectLoaderStatus.LOADING))
                {
                    promise.complete(ProjectLoaderStatus.LOADING);
                }
                else if(projectLoaderStatus.equals(ProjectLoaderStatus.ERROR))
                {
                    promise.complete(ProjectLoaderStatus.ERROR);
                }
            }

            if (res.failed()) {
                promise.fail(res.cause());
            }

        });

        return promise.future();
    }

    private Future<Void> loadProject(ProjectLoader loader) {
        Promise<Void> promise = Promise.promise();
        List<String> oriUUIDList = loader.getUuidListFromDb();
        loader.setDbOriUUIDSize(oriUUIDList.size());

        for (int i = 0; i < oriUUIDList.size(); ++i) {
            final Integer currentLength = i + 1;
            final String UUID = oriUUIDList.get(i);
            Tuple params = Tuple.of(loader.getProjectId(), UUID);

            queryOps.runQuery(AnnotationQuery.getLoadValidImageProjectUuid(), params, annotationPool)
                    .onComplete(DBUtils.handleResponse(
                            result -> {
                                if(result.iterator().hasNext())
                                {
                                    Row row = result.iterator().next();

                                    String dataSubPath = row.getString(0);
                                    File dataFullPath = loader.getDataFullPath(dataSubPath);

                                    if (ImageHandler.isImageReadable(dataFullPath))
                                    {
                                        loader.pushDBValidUUID(UUID);
                                    }
                                }
                                loader.updateDBLoadingProgress(currentLength);
                                if(!promise.future().isComplete()) {
                                    promise.complete();
                                }
                            },
                            promise::fail
                    ));
        }
        return promise.future();
    }

    @Override
    public Future<String> renameData(@NonNull ProjectLoader projectLoader, String uuid, String newFileName) {
        RenameProjectData renameProjectData = new RenameProjectData(projectLoader);
        renameProjectData.getAnnotationVersion(uuid);

        Promise<String> promise = Promise.promise();

        if(renameProjectData.containIllegalChars(newFileName)) {
            // Abort if filename contain illegal chars
            promise.fail(RenameDataErrorCode.FILENAME_CONTAIN_ILLEGAL_CHAR.toString());
        }

        String updatedFileName = renameProjectData.modifyFileNameFromCache(newFileName);
        File newDataPath = renameProjectData.createNewDataPath(updatedFileName);

        if(newDataPath.exists()) {
            // Abort if name exists
            promise.fail(RenameDataErrorCode.FILENAME_EXIST.toString());
        }

        Tuple params = Tuple.of(updatedFileName, uuid, projectLoader.getProjectId());

        if(renameProjectData.renameDataPath(newDataPath, renameProjectData.getOldDataFileName()))
        {
            return queryOps.runQuery(AnnotationQuery.getRenameImageProjectData(), params, annotationPool)
                    .map(result -> {
                        renameProjectData.updateAnnotationCache(updatedFileName, uuid);
                        return newDataPath.toString();
                    });
        }

        if(!promise.future().isComplete()) {
            promise.fail(RenameDataErrorCode.RENAME_FAIL.toString());
        }

        return promise.future();
    }

    private List<ThumbnailProperties> parseBoundingBoxProperties(String jsonList) throws JsonProcessingException {
        return new ObjectMapper().readValue(jsonList, new TypeReference<>() {});
    }

    private String writeJsonString(Object object) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(object);
    }

    @Override
    public Future<ThumbnailProperties> getThumbnail(@NonNull ProjectLoader projectLoader, @NonNull String uuid) {
        Promise<ThumbnailProperties> promise = Promise.promise();
        String annotationKey = AnnotationType.get(projectLoader.getAnnotationType()).name();
        promise.complete(queryData(projectLoader, uuid, annotationKey));

        return promise.future();
    }

    @Override
    public Future<String> getImageSource(@NonNull ProjectLoader projectLoader, @NonNull String uuid) {
        Tuple params = Tuple.of(uuid, projectLoader.getProjectId());

        return queryOps.runQuery(AnnotationQuery.getRetrieveDataPathFromImageProject(), params, annotationPool)
                .map(result -> {
                    if(result.size() != 0) {
                        Row row = result.iterator().next();
                        String dataPath = row.getString(0);
                        File fileImgPath = projectLoader.getDataFullPath(dataPath);
                        return ImageHandler.encodeFileToBase64Binary(fileImgPath);
                    }

                    log.info("Failure to retrieve data path for " + projectLoader.getProjectName() + " with uuid " + uuid);
                    return null;
                });
    }

    private ThumbnailProperties queryData(@NonNull ProjectLoader loader, String uuid, @NonNull String annotationKey)
    {
        ImageDTO annotation = loader.getUuidAnnotationDict().get(uuid);
        DataInfoProperties version = annotation.getAnnotationDict().get(loader.getCurrentVersionUuid());
        Map<String, String> imgData = new HashMap<>();
        String dataPath = Paths.get(loader.getProjectPath().getAbsolutePath(), annotation.getImgPath()).toString();

        try
        {
            imgData = ImageHandler.getImageMetaData(new File(dataPath));
        }
        catch(Exception e)
        {
            log.debug("Failure in reading image of path " + dataPath, e);
        }

        ThumbnailProperties thmbProps = ThumbnailProperties.builder()
                .message(1)
                .uuidParam(uuid)
                .projectNameParam(loader.getProjectName())
                .imgPathParam(dataPath)
                .imgDepth(Integer.parseInt(imgData.get(ParamConfig.getImgDepth())))
                .imgXParam(version.getImgX())
                .imgYParam(version.getImgY())
                .imgWParam(version.getImgW())
                .imgHParam(version.getImgH())
                .fileSizeParam(annotation.getFileSize())
                .imgOriWParam(Integer.parseInt(imgData.get(ParamConfig.getImgOriWParam())))
                .imgOriHParam(Integer.parseInt(imgData.get(ParamConfig.getImgOriHParam())))
                .imgThumbnailParam(imgData.get(ParamConfig.getBase64Param()))
                .build();

        if(annotationKey.equals(AnnotationType.IMAGEBOUNDINGBOX.name())) {
            thmbProps.setBoundingBoxParam(new ArrayList<>(version.getAnnotation()));
        } else if(annotationKey.equals(AnnotationType.IMAGESEGMENTATION.name())) {
            thmbProps.setSegmentationParam(new ArrayList<>(version.getAnnotation()));
        }

        return thmbProps;
    }

    @Override
    public void configProjectLoaderFromDb(@NonNull ProjectLoader loader)
    {
        queryOps.runQuery(loader, AnnotationQuery.getExtractImageProject(), Tuple.of(loader.getProjectId()),
                annotationPool, DBUtils.handleResponse(
                result -> {
                    if (result.size() == 0) {
                        log.info("Extract project annotation retrieve 0 rows. Project not found from project database");
                    } else {

                        for (Row row : result) {
                            String fullPath = loader.getDataFullPath(row.getString(1)).toString();

                            if (ImageHandler.isImageReadable(new File(fullPath))) {
                                Map<String, DataInfoProperties> annotationDict = ProjectParser.buildAnnotationDict(row.getString(2));

                                ImageDTO annotation = ImageDTO.builder()
                                        .uuid(row.getString(0))         //uuid
                                        .projectId(loader.getProjectId())    //project_id
                                        .imgPath(row.getString(1))      //img_path
                                        .annotationDict(annotationDict)      //version_list
                                        .imgDepth(row.getInteger(3))    //img_depth
                                        .imgOriW(row.getInteger(4))     //img_ori_w
                                        .imgOriH(row.getInteger(5))     //img_ori_h
                                        .fileSize(row.getLong(6))    //file_size
                                        .build();

                                loader.getUuidAnnotationDict().put(row.getString(0), annotation);
                            } else {
                                //remove uuid which is not readable
                                loader.getSanityUuidList().remove(row.getString(0));
                            }
                        }
                    }
                },
                cause -> log.info("Error query for config loader from db")
        ));
    }

//    public Future<Void> exportProject(String projectId, int exportType) {
//        Tuple params = Tuple.of(projectId);
//        Promise<Void> promise = Promise.promise();
//        queryOps.runQuery(PortfolioDbQuery.getExportProject(), params, annotationPool)
//                .onComplete(DBUtils.handleResponse(
//                        result -> {
//                            // export project table relevant
//                            ProjectLoader loader = projectHandler.getProjectLoader(projectId);
//                            JDBCPool client = holder.getJDBCPool(Objects.requireNonNull(loader));
//
//                            client.preparedQuery(AnnotationQuery.getExtractProject())
//                                    .execute(params)
//                                    .onComplete(annotationFetch -> {
//                                        if (annotationFetch.succeeded())
//                                        {
//                                            ProjectConfigProperties configContent = projectExport.getConfigContent(result,
//                                                    annotationFetch.result());
//                                            if(configContent == null) return;
//
//                                            fileGenerator.run(projectExport, loader, configContent, exportType);
//                                        }
//                                    });
//                            promise.complete();
//                        },
//                        cause -> {
//                            projectExport.setExportStatus(ProjectExport.ProjectExportStatus.EXPORT_FAIL);
//                            log.info("Project export fail", cause);
//                        }
//                ));
//
//        return promise.future();
//    }

}
