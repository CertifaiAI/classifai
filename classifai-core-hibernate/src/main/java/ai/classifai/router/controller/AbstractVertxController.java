package ai.classifai.router.controller;

import io.vertx.core.Vertx;

public abstract class AbstractVertxController extends AbstractController
{
    protected Vertx vertx;

    public AbstractVertxController(Vertx vertx)
    {
        this.vertx = vertx;
    }

//    protected Future<Void> renameProject(Project project, String newName)
//    {
//        JsonObject request = new JsonObject()
//                .put("project", project)
//                .put(ParamConfig.getNewProjectNameParam(), newName);
//
//        DeliveryOptions options = getDeliveryOptions(DbAction.RENAME_PROJECT.name());
//
//        return vertx.eventBus().request(DatabaseVerticle.QUEUE, request, options)
//                .compose(msg -> Future.succeededFuture().mapEmpty());
//    }
//
//    protected Future<Void> starProjectFuture(Project project, Boolean isStarred)
//    {
//        JsonObject request = new JsonObject()
//                .put("project", project)
//                .put(ParamConfig.getIsStarredParam(), isStarred);
//
//        DeliveryOptions options = getDeliveryOptions(DbAction.STAR_PROJECT.name());
//
//        return vertx.eventBus().request(DatabaseVerticle.QUEUE, request, options)
//                .compose(msg -> Future.succeededFuture().mapEmpty());
//    }
//
//    protected Future<Void> deleteProjectFuture(Project project)
//    {
//        JsonObject request = new JsonObject()
//                .put("project", project);
//
//        DeliveryOptions options = getDeliveryOptions(DbAction.DELETE_PROJECT.name());
//
//        return vertx.eventBus().request(DatabaseVerticle.QUEUE, request, options)
//                .compose(msg -> Future.succeededFuture().mapEmpty());
//    }
//
//    private Future<Optional<Project>> findProjectByNameAndAnnotationFuture(AnnotationType annotationType, String projectName)
//    {
//        JsonObject request = new JsonObject()
//                .put(ParamConfig.getAnnotationTypeParam(), annotationType)
//                .put(ParamConfig.getProjectNameParam(), projectName);
//
//        DeliveryOptions options = getDeliveryOptions(DbAction.FIND_PROJECT_BY_ANNOTATION_AND_NAME.name());
//
//        return vertx.eventBus().<Optional<Project>>request(DatabaseVerticle.QUEUE, request, options)
//                .compose(msg -> Future.succeededFuture(msg.body()));
//    }
//    protected Future<Project> getProjectByNameAndAnnotation(AnnotationType annotationType, String projectName)
//    {
//        return findProjectByNameAndAnnotationFuture(annotationType, projectName)
//                .compose(optional ->
//                {
//                    if (optional.isEmpty())
//                    {
//                        return Future.failedFuture(String.format("Unable to find project [%s] %s", annotationType.name(), projectName));
//                    }
//
//                    return Future.succeededFuture(optional.get());
//                });
//    }
//
//    protected Future<Void> projectNameAndAnnotationAvailable(AnnotationType annotationType, String projectName)
//    {
//        return findProjectByNameAndAnnotationFuture(annotationType, projectName)
//                .compose(optional ->
//                {
//                    if (optional.isEmpty())
//                    {
//                        return Future.succeededFuture();
//                    }
//
//                    return Future.failedFuture(String.format("Project [%s] %s already exists!"));
//                });
//    }
//
//    protected Future<List<Project>> listProjectsByAnnotation(AnnotationType annotationType)
//    {
//        JsonObject request = new JsonObject()
//                .put(ParamConfig.getAnnotationTypeParam(), annotationType);
//
//        DeliveryOptions options = getDeliveryOptions(DbAction.LIST_PROJECTS_BY_ANNOTATION.name());
//
//        return vertx.eventBus().<JsonObject>request(DatabaseVerticle.QUEUE, request, options)
//                .compose(msg ->
//                {
//                    List<Project> projectList = (List<Project>) msg.body().getValue("project_list");
//                    return Future.succeededFuture(projectList);
//                });
//    }
//
//    protected Future<Void> deleteLabelListFuture(List<Label> labelList)
//    {
//        JsonObject request = new JsonObject()
//                .put("label_list", labelList);
//
//        DeliveryOptions options = getDeliveryOptions(DbAction.DELETE_LABEL_LIST.name());
//
//        return vertx.eventBus().<Void>request(DatabaseVerticle.QUEUE, request, options)
//                .compose(msg -> Future.succeededFuture(msg.body()));
//    }
//
//    protected Future<Void> addLabelDTOListFuture(List<LabelDTO> labelDTOS)
//    {
//        JsonObject request = new JsonObject()
//                .put("label_dto_list", labelDTOS);
//
//        DeliveryOptions options = getDeliveryOptions(DbAction.CREATE_LABEL_LIST.name());
//
//        return vertx.eventBus().<Void>request(DatabaseVerticle.QUEUE, request, options)
//                .compose(msg -> Future.succeededFuture(msg.body()));
//    }
//
//    protected Future<DataVersion> getDataVersionFuture(RoutingContext context)
//    {
//        JsonObject request = paramHandler.dataParamToJson(context);
//
//        DeliveryOptions options = getDeliveryOptions(DbAction.GET_IMAGE_DATA_VERSION.name());
//
//        return vertx.eventBus().<DataVersion>request(DatabaseVerticle.QUEUE, request, options)
//                .compose(msg -> Future.succeededFuture(msg.body()));
//    }
//
//    protected Future<Data> getData(UUID dataId)
//    {
//        JsonObject request = new JsonObject()
//                .put("data_id", dataId);
//
//        DeliveryOptions options = getDeliveryOptions(DbAction.GET_DATA.name());
//
//        return vertx.eventBus().<Data>request(DatabaseVerticle.QUEUE, request, options)
//                .compose(msg -> Future.succeededFuture(msg.body()));
//    }
//
//    protected Future<Void> deleteAnnotationListFuture(List<Annotation> imageAnnotations)
//    {
//        JsonObject request = new JsonObject()
//                .put("image_annotation_list", imageAnnotations);
//
//        DeliveryOptions options = getDeliveryOptions(DbAction.DELETE_IMAGE_ANNOTATION_LIST.name());
//
//        return vertx.eventBus().<Void>request(DatabaseVerticle.QUEUE, request, options)
//                .compose(msg -> Future.succeededFuture(msg.body()));
//    }
//
//    protected Future<Void> updateAnnotationListFuture(List<Annotation> imageAnnotations)
//    {
//        JsonObject request = new JsonObject()
//                .put("image_annotation_list", imageAnnotations);
//
//        DeliveryOptions options = getDeliveryOptions(DbAction.UPDATE_IMAGE_ANNOTATION_LIST.name());
//
//        return vertx.eventBus().<Void>request(DatabaseVerticle.QUEUE, request, options)
//                .compose(msg -> Future.succeededFuture(msg.body()));
//    }
//
//    protected Future<Void> addAnnotationDTOListFuture(List<AnnotationDTO> imageAnnotationDTOList)
//    {
//        JsonObject request = new JsonObject()
//                .put("image_annotation_list", imageAnnotationDTOList);
//
//        DeliveryOptions options = getDeliveryOptions(DbAction.ADD_IMAGE_ANNOTATION_LIST.name());
//
//        return vertx.eventBus().<Void>request(DatabaseVerticle.QUEUE, request, options)
//                .compose(msg -> Future.succeededFuture(msg.body()));
//    }
//
//    protected Future<Void> updateDataVersionFuture(DataVersion imageDataVersion, DataVersionDTO imageDataVersionDTO)
//    {
//        JsonObject request = new JsonObject()
//                .put("image_data_version", imageDataVersion)
//                .put("image_data_version_dto", imageDataVersionDTO);
//
//        DeliveryOptions options = getDeliveryOptions(DbAction.UPDATE_IMAGE_DATA_VERSION.name());
//
//        return vertx.eventBus().<Void>request(DatabaseVerticle.QUEUE, request, options)
//                .compose(msg -> Future.succeededFuture(msg.body()));
//    }
//
//    protected Future<List<Data>> addDataDTOList(List<DataDTO> dataDTOS)
//    {
//        JsonObject request = new JsonObject()
//                .put("data_dto_list", dataDTOS);
//
//        DeliveryOptions options = getDeliveryOptions(DbAction.ADD_DATA_LIST.name());
//
//        return vertx.eventBus().<List<Data>>request(DatabaseVerticle.QUEUE, request, options)
//                .compose(msg -> Future.succeededFuture(msg.body()));
//    }
//
//    protected Future<Void> createProject(ProjectDTO projectDTO, VersionDTO versionDTO, List<ImageDataDTO> dataDTOS, List<LabelDTO> labelDTOS, List<ImageDataVersionDTO> dataVersionDTOS)
//    {
//        JsonObject request = new JsonObject()
//                .put("project_dto", projectDTO)
//                .put("version_dto", versionDTO)
//                .put("data_dto_list", dataDTOS)
//                .put("label_dto_list", labelDTOS)
//                .put("data_version_dto_list", dataVersionDTOS);
//
//        DeliveryOptions options = getDeliveryOptions(DbAction.CREATE_PROJECT.name());
//
//        return vertx.eventBus().<Void>request(DatabaseVerticle.QUEUE, request, options)
//                .compose(msg -> Future.succeededFuture(msg.body()));
//    }
}
