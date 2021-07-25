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
package ai.classifai.database;

import io.vertx.core.*;
import lombok.extern.slf4j.Slf4j;

/**
 * verticle that handles database
 *
 * @author YCCertifai
 */
@Slf4j
public class DatabaseVerticle extends AbstractVerticle
{

//    //    ***********************************PROJECT*********************************************
//    private void findProjectByAnnotationAndName(Message<JsonObject> message)
//    {
//        AnnotationType annotationType = getAnnotationType(message);
//        String projectName = getProjectName(message);
//
//        vertx.executeBlocking(promise ->
//                promise.complete(projectRepository.findByNameAndType(projectName, annotationType.ordinal())))
//                .onSuccess(message::<Optional<Project>>replyAndRequest)
//                .onFailure(dbErrorHandler(message));
//    }
//
//
//    private void listProjectsByAnnotation(Message<JsonObject> message)
//    {
//        AnnotationType annotationType = getAnnotationType(message);
//
//        vertx.executeBlocking(promise ->
//                promise.complete(projectRepository.listByType(annotationType.ordinal())))
//                .onSuccess(projectList -> message.replyAndRequest(new JsonObject().put("project_list", projectList)))
//                .onFailure(dbErrorHandler(message));
//    }
//
//    private void starProject(Message<JsonObject> message)
//    {
//        Project project = getProject(message);
//        Boolean isStarred = getStarStatus(message);
//
//        executeBlocking(message, projectRepository.star(project, isStarred));
//    }
//
//    private void createImageProject(Message<JsonObject> message)
//    {
//        ProjectDTO projectDTO = getProjectDTO(message);
//        VersionDTO versionDTO = getVersionDTO(message);
//        List<ImageDataDTO> ImageDataDTOList = getImageDataDTOList(message);
//        List<ImageDataVersionDTO> ImageDataVersionDTO = getImageDataVersionDTOList(message);
//        List<LabelDTO> LabelDTOList = getLabelDTOList(message);
//
//        vertx.executeBlocking(promise -> promise.complete(projectRepository.create(projectDTO)))
//
//    }
//
//    private List<LabelDTO> getLabelDTOList(Message<JsonObject> message) {
//    }
//
//    private List<ImageDataVersionDTO> getImageDataVersionDTOList(Message<JsonObject> message) {
//    }
//
//    private List<ImageDataDTO> getImageDataDTOList(Message<JsonObject> message) {
//    }
//
//    private VersionDTO getVersionDTO(Message<JsonObject> message) {
//    }
//
//    private ProjectDTO getProjectDTO(Message<JsonObject> message) {
//    }

//    private void importProject(Message<JsonObject> message)
//    {
//    }
//
//    private void exportProject(Message<JsonObject> message)
//    {
//    }
//
//    private void closeProjectState(Message<JsonObject> message)
//    {
//        ProjectRepository repository = getProjectRepository();
//
//        int annotationTypeIdx = getAnnotationTypeIdx(message);
//        String projectName = getProjectName(message);
//
//        Handler<Promise<ProjectEntity>> getProjectHandler = promise ->
//                getProject(promise, projectName, annotationTypeIdx, repository);
//
//        Function<ProjectEntity, Future<ProjectEntity>> closeProjectStateHandler = this::setProjectCloseState;
//
//        Function<ProjectEntity, Future<Void>> persistProjectHandler = project ->
//                vertx.executeBlocking(promise -> persistProject(promise, project, repository));
//
//        vertx.executeBlocking(getProjectHandler)
//                .compose(closeProjectStateHandler)
//                .compose(persistProjectHandler)
//                .onSuccess(unused -> message.replyAndRequest(ReplyHandler.getOkReply()))
//                .onFailure(throwable -> message.replyAndRequest(ReplyHandler.reportUserDefinedError(throwable.getMessage())))
//                .onComplete(unused -> Repository.closeRepositories(repository));
//    }
//
//    private Future<ProjectEntity> setProjectCloseState(ProjectEntity project) {
//        Promise<ProjectEntity> promise = Promise.promise();
//        ProjectEntity.LOADED_PROJECT_LIST.remove(project);
//        promise.complete(project);
//        return promise.future();
//    }
//
//    private void renameProject(Message<JsonObject> message)
//    {
//        ProjectRepository repository = getProjectRepository();
//
//        int annotationTypeIdx = getAnnotationTypeIdx(message);
//        String projectName = getProjectName(message);
//        String newProjectName = getNewProjectName(message);
//
//        Handler<Promise<ProjectEntity>> getProjectHandler = promise ->
//                getProject(promise, projectName, annotationTypeIdx, repository);
//
//        Function<ProjectEntity, Future<ProjectEntity>> renameProjectHandler = project ->
//                vertx.executeBlocking(promise -> renameProject(promise, project, newProjectName));
//
//        Function<ProjectEntity, Future<Void>> persistProjectHandler = project ->
//                vertx.executeBlocking(promise -> persistProject(promise, project, repository));
//
//        vertx.executeBlocking(getProjectHandler)
//                .compose(renameProjectHandler)
//                .compose(persistProjectHandler)
//                .onSuccess(unused -> message.replyAndRequest(ReplyHandler.getOkReply()))
//                .onFailure(throwable -> message.replyAndRequest(ReplyHandler.reportUserDefinedError(throwable.getMessage())))
//                .onComplete(unused -> Repository.closeRepositories(repository));
//    }
//
//    private void updateData(Message<JsonObject> message) {
//
//        ProjectRepository projectRepository = getProjectRepository();
//        DataVersionRepository dataVersionRepository = getDataVersionRepository();
//        AnnotationRepository annotationRepository = getAnnotationRepository();
//
//        int annotationTypeIdx = getAnnotationTypeIdx(message);
//        String projectName = getProjectName(message);
//        String uuid = getUuid(message);
//
//        JsonObject request = message.body();
//
//        Handler<Promise<ProjectEntity>> getProjectHandler = promise ->
//                getProject(promise, projectName, annotationTypeIdx, projectRepository);
//
//        Function<ProjectEntity, Future<DataEntity>> getDataHandler = project ->
//                vertx.executeBlocking(promise -> getData(promise, project, projectName, annotationTypeIdx, uuid));
//
//        Function<DataEntity, Future<Pair<List<AnnotationEntity>, DataVersionEntity>>> mergeDataVersionHandler = data ->
//                vertx.executeBlocking(promise -> mergeAnnotationListPair(promise, data, request));
//
//        Function<Pair<List<AnnotationEntity>, DataVersionEntity>, Future<Void>> persistDataVersion = pair ->
//                vertx.executeBlocking(promise -> persistDataVersion(promise, pair, dataVersionRepository, annotationRepository));
//
//        // getProject
//        // getData
//        // mergeDataVersion
//        // persist
//        vertx.executeBlocking(getProjectHandler)
//                .compose(getDataHandler)
//                .compose(mergeDataVersionHandler)
//                .compose(persistDataVersion)
//                .onSuccess(unused -> message.replyAndRequest(ReplyHandler.getOkReply()))
//                .onFailure(throwable -> message.replyAndRequest(ReplyHandler.reportUserDefinedError(throwable.getMessage())))
//                .onComplete(unused -> Repository.closeRepositories(projectRepository, dataVersionRepository, annotationRepository));
//    }
//
//
//
//    // FIXME: need extra api for delete label
////    private void updateLabel(Message<JsonObject> message) {
////        ProjectRepository projectRepository = getProjectRepository();
////        LabelRepository labelRepository = getLabelRepository();
////
////        int annotationTypeIdx = getAnnotationTypeIdx(message);
////        String name = getName(message);
////        List<String> strLabelList = getStrLabelList(message);
////
////        Handler<Promise<Project>> getProjectHandler = promise ->
////                getProject(promise, name, annotationTypeIdx, projectRepository);
////
////        Function<Project, Future<List<Label>>> mergeLabelListHandler = project ->
////                vertx.executeBlocking(promise -> mergeLabelList(promise, project, strLabelList));
////
////        Function<List<Label>, Future<Void>> persistLabelListHandler = labelList ->
////                vertx.executeBlocking(promise -> persistLabelList(promise, labelList, labelRepository));
////
////        // get project
////        // merge label list
////        // persist
////        vertx.executeBlocking(getProjectHandler)
////                .compose(mergeLabelListHandler)
////                .compose(persistLabelListHandler)
////                .onSuccess(unused -> message.replyAndRequest(ReplyHandler.getOkReply()))
////                .onFailure(throwable -> message.replyAndRequest(ReplyHandler.reportUserDefinedError(throwable.getMessage())))
////                .onComplete(unused -> Repository.closeRepositories(projectRepository, labelRepository));
////    }
//
//    // temporary solution
//    private void updateLabel(Message<JsonObject> message) {
//        ProjectRepository projectRepository = getProjectRepository();
//        LabelRepository labelRepository = getLabelRepository();
//
//        int annotationTypeIdx = getAnnotationTypeIdx(message);
//        String projectName = getProjectName(message);
//        List<String> strLabelList = getStrLabelList(message);
//
//        Handler<Promise<ProjectEntity>> getProjectHandler = promise ->
//                getProject(promise, projectName, annotationTypeIdx, projectRepository);
//
//        Function<ProjectEntity, Future<Pair<List<LabelEntity>, List<LabelEntity>>>> mergeLabelListHandler = project ->
//                vertx.executeBlocking(promise -> mergeLabelList(promise, project, strLabelList));
//
//        Function<Pair<List<LabelEntity>, List<LabelEntity>>, Future<Void>> persistLabelListHandler = pair ->
//                vertx.executeBlocking(promise -> persistLabelListPair(promise, pair, labelRepository));
//
//        // get project
//        // merge label list
//        // persist
//        vertx.executeBlocking(getProjectHandler)
//                .compose(mergeLabelListHandler)
//                .compose(persistLabelListHandler)
//                .onSuccess(unused -> message.replyAndRequest(ReplyHandler.getOkReply()))
//                .onFailure(throwable -> message.replyAndRequest(ReplyHandler.reportUserDefinedError(throwable.getMessage())))
//                .onComplete(unused -> Repository.closeRepositories(projectRepository, labelRepository));
//    }
//
//    // call load project
//    private void reloadProject(Message<JsonObject> message) {
//        ProjectRepository projectRepository = getProjectRepository();
//        DataRepository dataRepository = getDataRepository();
//
//        int annotationTypeIdx = getAnnotationTypeIdx(message);
//        String projectName = getProjectName(message);
//
//        Handler<Promise<ProjectEntity>> getProjectHandler = promise ->
//                getProject(promise, projectName, annotationTypeIdx, projectRepository);
//
//        Function<ProjectEntity, Future<List<DataEntity>>> mergeDataListHandler = project ->
//                vertx.executeBlocking(promise -> getNewlyAddedDataList(promise, project));
//
//        Function<List<DataEntity>, Future<List<DataEntity>>> persistDataListHandler = dataList ->
//                vertx.executeBlocking(promise -> persistDataList(promise, dataList, dataRepository));
//
//        Handler<List<DataEntity>> successHandler = dataList ->
//        {
//            JsonObject jsonObj = new JsonObject();
//            jsonObj.put("uuid_add_list", DataHandler.getDataIdList(dataList));
//            message.replyAndRequest(jsonObj);
//        };
//
//        // get project
//        // merge dataList
//        // persist
//        vertx.executeBlocking(getProjectHandler)
//                .compose(mergeDataListHandler)
//                .compose(persistDataListHandler)
//                .onSuccess(successHandler)
//                .onFailure(throwable -> message.replyAndRequest(ReplyHandler.reportUserDefinedError(throwable.getMessage())))
//                .onComplete(unused -> Repository.closeRepositories(projectRepository, dataRepository));
//    }
//
//    private void deleteProject(Message<JsonObject> message) {
//        ProjectRepository projectRepository = getProjectRepository();
//
//        int annotationTypeIdx = getAnnotationTypeIdx(message);
//        String projectName = getProjectName(message);
//
//        Handler<Promise<ProjectEntity>> getProjectHandler = promise -> getProject(promise, projectName, annotationTypeIdx, projectRepository);
//
//        Function<ProjectEntity, Future<Void>> deleteProjectHandler = project ->
//                vertx.executeBlocking(promise -> deleteProject(promise, project, projectRepository));
//
//        vertx.executeBlocking(getProjectHandler)
//                .compose(deleteProjectHandler)
//                .onSuccess(unused -> message.replyAndRequest(ReplyHandler.getOkReply()))
//                .onFailure(throwable -> message.replyAndRequest(ReplyHandler.reportUserDefinedError(throwable.getMessage())))
//                .onComplete(unused -> Repository.closeRepositories(projectRepository));
//    }
//
//    private void starProject(Message<JsonObject> message) {
//        ProjectRepository repository = getProjectRepository();
//
//        boolean isStarred = getStarStatus(message);
//        int annotationTypeIdx = getAnnotationTypeIdx(message);
//        String projectName = getProjectName(message);
//
//        Handler<Promise<ProjectEntity>> getProjectHandler = promise -> getProject(promise, projectName, annotationTypeIdx, repository);
//
//        Function<ProjectEntity, Future<Void>> starProjectHandler = project ->
//                vertx.executeBlocking(promise -> starProject(promise, project, isStarred, repository));
//
//        vertx.executeBlocking(getProjectHandler)
//                .compose(starProjectHandler)
//                .onSuccess(unused -> message.replyAndRequest(ReplyHandler.getOkReply()))
//                .onFailure(reportDbError(message))
//                .onComplete(unused -> Repository.closeRepositories(repository));
//    }
//
//
//    private void getDataSource(Message<JsonObject> message) {
//        ProjectRepository repository = getProjectRepository();
//
//        int annotationTypeIdx = getAnnotationTypeIdx(message);
//        String projectName = getProjectName(message);
//        String uuid = getUuid(message);
//
//
//        Handler<Promise<ProjectEntity>> getProjectHandler = promise -> getProject(promise, projectName, annotationTypeIdx, repository);
//
//        Function<ProjectEntity, Future<DataEntity>> getDataHandler = project ->
//                vertx.executeBlocking(promise -> getData(promise, project, projectName, annotationTypeIdx, uuid));
//
//        Function<DataEntity, Future<String>> getImgSourceHandler = image ->
//                vertx.executeBlocking(promise -> getDataSource(promise, image));
//
//        Handler<String> successHandler = imgSource ->
//        {
//            JsonObject jsonObj = ReplyHandler.getOkReply();
//
//            jsonObj.put("img_src", imgSource);
//
//            message.replyAndRequest(jsonObj);
//        };
//
//        vertx.executeBlocking(getProjectHandler)
//                .compose(getDataHandler)
//                .compose(getImgSourceHandler) // can send to other verticles in future
//                .onSuccess(successHandler)
//                .onFailure(throwable -> message.replyAndRequest(ReplyHandler.reportUserDefinedError(throwable.getCause().getMessage())))
//                .onComplete(unused -> Repository.closeRepositories(repository));
//    }
//
//    // FIXME: is this only for image
//    private void getThumbnail(Message<JsonObject> message) {
//        // store object due to reference passing throughout multiple composes
//        final JsonObject STORE = new JsonObject();
//
//        ProjectRepository repository = getProjectRepository();
//
//        int annotationTypeIdx = getAnnotationTypeIdx(message);
//        String projectName = getProjectName(message);
//        String uuid = getUuid(message);
//
//        Handler<Promise<ProjectEntity>> getProjectHandler = promise ->
//                getProject(promise, projectName, annotationTypeIdx, repository);
//
//        Function<ProjectEntity, Future<DataEntity>> getDataHandler = project ->
//                vertx.executeBlocking(promise -> getData(promise, project, projectName, annotationTypeIdx, uuid));
//
//        Function<Void, Future<String>> getThumbnailHandler = unused ->
//                vertx.executeBlocking(promise -> getThumbnail(promise, (ImageDataEntity) STORE.getValue("image")));
//
//        Handler<String> successHandler = thumbnail ->
//        {
//            JsonObject jsonObj = ReplyHandler.getOkReply();
//
//            DataEntity data = (DataEntity) STORE.getValue("image");
//
//            jsonObj.mergeIn(data.loadData());
//
//            jsonObj.put("img_thumbnail", thumbnail);
//
//            message.replyAndRequest(jsonObj);
//
//        };
//
//        vertx.executeBlocking(getProjectHandler)
//                .compose(getDataHandler)
//                .compose(img -> storeObject(STORE, "image", img))
//                .compose(getThumbnailHandler) // can send to other verticles in future
//                .onSuccess(successHandler)
//                .onFailure(throwable -> message.replyAndRequest(ReplyHandler.reportUserDefinedError(throwable.getCause().getMessage())))
//                .onComplete(unused -> Repository.closeRepositories(repository));
//    }
//
//    // FIXME: perform image validation here!!!!!
//    private void loadProject(Message<JsonObject> message) {
//        ProjectRepository repository = getProjectRepository();
//
//        int annotationTypeIdx = getAnnotationTypeIdx(message);
//        String projectName = getProjectName(message);
//
//        Handler<Promise<ProjectEntity>> getProjectHandler = promise ->
//                getProject(promise, projectName, annotationTypeIdx, repository);
//
//        Handler<ProjectEntity> successHandler = project ->
//        {
//            JsonObject jsonObj = ReplyHandler.getOkReply();
//
//            jsonObj.put("label_list", LabelHandler.getStringListFromLabelList(project.getProjectCurrentVersion().getLabelList()));
//
//            List<String> uuidList = project.getDataList().stream()
//                    .map(data -> data.getId().toString())
//                    .collect(Collectors.toList());
//
//            jsonObj.put("uuid_list", uuidList);
//
//            ProjectEntity.LOADED_PROJECT_LIST.add(project);
//
//            message.replyAndRequest(jsonObj);
//        };
//
//        vertx.executeBlocking(getProjectHandler)
//                .onSuccess(successHandler)
//                .onFailure(reportDbError(message))
//                .onComplete(unused -> Repository.closeRepositories(repository));
//    }
//
//    private void createImageProject(Message<JsonObject> message)
//    {
//        JsonObject msgBody = message.body();
//
//        int annotationTypeIdx = getAnnotationTypeIdx(message);
//        String projectName = getProjectName(message);
//
//        String projectPath = msgBody.getString(ParamConfig.getProjectPathParam());
//        String labelPath = msgBody.getString(ParamConfig.getLabelPathParam());
//
//        Handler<Promise<ProjectEntity>> createImageProject = promise ->
//        {
//            // create new project
//
//            ProjectEntity project = ProjectHandler.buildNewProject(projectName, annotationTypeIdx, projectPath, labelPath);
//
//            projectRepository.saveProject(project);
//
//            promise.complete(project);
//
//        };
//
//        Handler<ProjectEntity> successHandler = project ->
//        {
//            JsonObject jsonObj = ReplyHandler.getOkReply();
//
//            List<JsonObject> metaList = new ArrayList<>();
//            metaList.add(project.getMeta());
//
//            jsonObj.put("content", metaList);
//
//            message.replyAndRequest(jsonObj);
//        };
//
//
////        startTransactionFuture()
////                .compose(createImageProject)
////                .compose(storeProject)// store to create data
////                .compose(createVersion)
////                .compose(storeVersion)// store to create label and dataversion
////                .compose(processLabelFile)
////                .compose(createLabelList)
////                .compose(processProjectPath)
////                .compose(createDataList)// do not store because can pass down to data version list
////                .compose(createDataVersionList)
////                .compose(unused -> commitTransactionFuture())
////                .onSuccess(outputProjectAsMeta)
////                .onFailure()
////                .onFailure()
//        // checkProjectNameAvailability
//        // checkProjectPathExists
//        // checkLabelPathExists
//
//
//
//
//
//    }
//
//
//
//
//    private void getProjectMeta(Message<JsonObject> message)
//    {
//        int annotationTypeIndex = getAnnotationTypeIdx(message);
//        String projectName = getProjectName(message);
//
//        Handler<Promise<ProjectEntity>> getProject = promise -> getProject(promise, projectName, annotationTypeIndex);
//
//        Handler<ProjectEntity> outputProjectAsMeta = project ->
//        {
//            JsonObject jsonObj = ReplyHandler.getOkReply();
//
//            List<JsonObject> metaList = Collections.singletonList(project.getMeta());
//
//            jsonObj.put("content", metaList); //TODO: hardcoded key
//
//            message.replyAndRequest(jsonObj);
//        };
//
//        vertx.executeBlocking(getProject)
//                .onSuccess(outputProjectAsMeta)
//                .onFailure(reportDbError(message));
//    }
//
//
//
//    private void mergeAnnotationListPair(Promise<Pair<List<AnnotationEntity>, DataVersionEntity>> promise, DataEntity data, JsonObject request)
//    {
//        DataVersionEntity dataVersion = data.getCurrentDataVersion();
//
//        dataVersion.updateDataFromJson(request);
//
//        AnnotationListFactory annotationListFactory = new AnnotationListFactory();
//
//        List<Annotation> annotationList = annotationListFactory.getAnnotationListFromJson(request, dataVersion);
//
//        AnnotationHandler annotationHandler = new AnnotationHandler();
//        List<AnnotationEntity> deleteList = annotationHandler.getDeleteList(annotationList, dataVersion.getAnnotations());
//
//        dataVersion.setAnnotations(annotationList);
//
//        promise.complete(new ImmutablePair<>(deleteList, dataVersion));
//    }
//
//    private void persistDataVersion(Promise<Void> promise, Pair<List<AnnotationEntity>, DataVersionEntity> annotationListPair,
//                                    DataVersionRepository repository, AnnotationRepository annotationRepository)
//    {
//        DataVersionEntity dataVersion = annotationListPair.getRight();
//
//        annotationRepository.removeAnnotationList(annotationListPair.getLeft());
//
//        repository.saveDataVersion(dataVersion);
//
//        promise.complete();
//    }
//
////    private void mergeDataVersion(Promise<DataVersion> promise, Data data, JsonObject request)
////    {
////        DataVersion dataVersion = data.getCurrentDataVersion();
////
////        dataVersion.updateDataFromJson(request);
////
////        promise.complete(dataVersion);
////    }
////
////    private void persistDataVersion(Promise<Void> promise, DataVersion dataVersion, DataVersionRepository repository)
////    {
////        repository.saveDataVersion(dataVersion);
////
////        promise.complete();
////    }
//
////    private void mergeLabelList(Promise<List<Label>> promise, Project project, List<String> strLabelList)
////    {
////        LabelHandler labelHandler = new LabelHandler();
////
////        List<Label> labelList = labelHandler.getLabelList(project, strLabelList);
////
////        promise.complete(labelList);
////    }
//
//    private void persistProject(Promise<Void> promise, ProjectEntity project, ProjectRepository repository)
//    {
//        repository.saveProject(project);
//
//        promise.complete();
//    }
//
//    private void renameProject(Promise<ProjectEntity> promise, ProjectEntity project, String newProjectName)
//    {
//        project.setName(newProjectName);
//        promise.complete(project);
//    }
//
//    // left: to delete, right: to udpate
//    private void mergeLabelList(Promise<Pair<List<LabelEntity>, List<LabelEntity>>> promise, ProjectEntity project, List<String> strLabelList)
//    {
//        LabelHandler labelHandler = new LabelHandler();
//
//        List<LabelEntity> deleteList = labelHandler.getDeleteList(project, strLabelList);
//        List<LabelEntity> labelList = labelHandler.getLabelList(project, strLabelList);
//
//        promise.complete(new ImmutablePair<>(deleteList, labelList));
//    }
//
//    private void persistLabelListPair(Promise<Void> promise, Pair<List<LabelEntity>, List<LabelEntity>> pair, LabelRepository repository)
//    {
//        repository.removeLabelList(pair.getLeft());
//
//        repository.saveLabelList(pair.getRight());
//
//        promise.complete();
//    }
//
//    private void persistLabelList(Promise<Void> promise, List<LabelEntity> labelList, LabelRepository repository)
//    {
//        repository.saveLabelList(labelList);
//
//        promise.complete();
//    }
//
//    private void persistDataList(Promise<List<DataEntity>> promise, List<DataEntity> dataList, DataRepository repository)
//    {
//        repository.saveDataList(dataList);
//
//        promise.complete(dataList);
//    }
//
//    private void getNewlyAddedDataList(Promise<List<DataEntity>> promise, ProjectEntity project)
//    {
//        DataHandler dataHandler = DataHandler.getDataHandler(project.getAnnotationType());
//
//        if (dataHandler == null)
//        {
//            promise.fail(String.format("Unable to identify annotation type for project %s", project.getName()));
//            return;
//        }
//
//        List<DataEntity> dataList = dataHandler.getNewlyAddedDataList(project);
//
//        promise.complete(dataList);
//    }
//
//    private void getDataSource(Promise<String> promise, DataEntity data)
//    {
//        DataHandler dataHandler = DataHandler.getDataHandler(data.getProject().getAnnotationType());
//        if (dataHandler == null)
//        {
//            promise.fail(String.format("Unable to identify annotation type for project %s", data.getProject().getName()));
//            return;
//        }
//
//        String dataSource = dataHandler.generateDataSource(data);
//
//        if (dataSource.length() == 0)
//        {
//            ProjectEntity project = data.getProject();
//
//            promise.fail(String.format("Failed to generate image source for data %s in project: [%s] %s",
//                    data.getFullPath(), AnnotationType.fromInt(project.getAnnotationType()).name(),
//                    project.getName()));
//            return;
//        }
//
//        promise.complete(dataSource);
//    }
//
//
//    // TODO: required further thought on API writing
//    private void getThumbnail(Promise<String> promise, ImageDataEntity image)
//    {
//        String thumbnail = new ImageHandler().generateThumbnail(image);
//
//        if (thumbnail.length() == 0)
//        {
//            ProjectEntity project = image.getProject();
//
//            promise.fail(String.format("Failed to generate thumbnail for data %s in project: [%s] %s",
//                    image.getFullPath(), AnnotationType.fromInt(project.getAnnotationType()).name(),
//                    project.getName()));
//            return;
//        }
//
//        promise.complete(thumbnail);
//    }
//
//    private void deleteProject(Promise<Void> promise, ProjectEntity project, ProjectRepository projectRepository)
//    {
//        projectRepository.deleteProject(project);
//        promise.complete();
//
//    }
//
//    private void getData(Promise<DataEntity> promise, ProjectEntity project, String projectName, Integer annotationTypeIdx, String uuid)
//    {
//        Optional<DataEntity> data = project.getDataList().stream()
//                .filter(d -> d.withId(uuid))
//                .findFirst();
//
//        if (data.isEmpty())
//        {
//            promise.fail(String.format("No entity found for data %s in project: [%s] %s",
//                    uuid, AnnotationType.values()[annotationTypeIdx], projectName));
//            return;
//        }
//
//        promise.complete(data.get());
//    }
//
//    private void starProject(Promise<Void> promise, ProjectEntity project, boolean isStarred, ProjectRepository repository) {
//        project.setStarred(isStarred);
//
//        repository.saveProject(project);
//
//        promise.complete();
//    }
//
//    private void getProject(Promise<ProjectEntity> promise, String projectName, int annotationTypeIdx)
//    {
//        Optional<ProjectEntity> project = projectRepository.findByAnnotationAndName(annotationTypeIdx, projectName);
//
//        if (project.isEmpty())
//        {
//            promise.fail(String.format("No entity found for project: [%s] %s",
//                    AnnotationType.values()[annotationTypeIdx], projectName));
//            return;
//        }
//
//        promise.complete(project.get());
//    }
//
//    private Future<Void> storeObject(JsonObject store, String key, Object obj)
//    {
//        store.put(key, obj);
//        return Future.succeededFuture();
//    }

//    //******************MESSAGE HANDLER***************************
//
//    private List<String> getStrLabelList(Message<JsonObject> message)
//    {
//        return StringHandler.stringToStringList(message.body().getString(ParamConfig.getLabelListParam()));
//    }
//
//
//
//    private String getUuid(Message<JsonObject> message)
//    {
//        return message.body().getString(ParamConfig.getUuidParam());
//    }
//
//    private String getNewProjectName(Message<JsonObject> message)
//    {
//        return message.body().getString(ParamConfig.getNewProjectNameParam());
//    }
//
//    private Boolean getStarStatus(Message<JsonObject> message)
//    {
//        return message.body().getBoolean(ParamConfig.getIsStarredParam());
//    }
//
//    private Project getProject(Message<JsonObject> message)
//    {
//        return (Project) message.body().getValue("project");
//    }
//
//    private String getProjectName(Message<JsonObject> message)
//    {
//        return message.body().getString(ParamConfig.getProjectNameParam());
//    }
//
//    private AnnotationType getAnnotationType(Message<JsonObject> message)
//    {
//        return AnnotationType.valueOf(message.body().getString(ParamConfig.getAnnotationTypeParam()));
//    }
//
//    //***************DB UTILS********************
//
//    private Handler<Throwable> dbErrorHandler(Message<JsonObject> message)
//    {
//        return throwable ->
//        {
//            rollbackTransaction();
//            message.fail(1, String.format("Database Error: %s", throwable.getMessage()));
//        };
//    }
//    //***************UNIT OF WORK****************
//
//
//    /**
//     * Async wrapper of startTransaction() method
//     *
//     * @return Future of startTransaction() method
//     */
//    private <T> Future<T> commitTransactionFuture(T object)
//    {
//        Handler<Promise<T>> commitTransaction = promise ->
//        {
//            commitTransaction();
//            promise.complete(object);
//        };
//
//        return vertx.executeBlocking(commitTransaction);
//    }
//
//    /**
//     * Async wrapper of startTransaction() method
//     *
//     * @return Future of startTransaction() method
//     */
//    private Future<Void> startTransactionFuture()
//    {
//        Handler<Promise<Void>> startTransaction = promise ->
//        {
//            startTransaction();
//            promise.complete();
//        };
//
//        return vertx.executeBlocking(startTransaction);
//    }
//
//    /**
//     * start transaction will only be used when performing create, update, delete.
//     * read is not required
//     */
//    private void startTransaction()
//    {
//        em.getTransaction().begin();
//    }
//
//    /**
//     * commit transaction will persist anything changed during the transaction
//     * call this method when process is succeeded
//     */
//    private void commitTransaction()
//    {
//        em.getTransaction().commit();
//    }
//
//    /**
//     * rollback transaction will revert all the changes during the transaction
//     * call this method when process is failed
//     */
//    private void rollbackTransaction()
//    {
//        em.getTransaction().rollback();
//    }

}
