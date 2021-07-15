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
package ai.classifai.db;

import ai.classifai.db.entities.LabelEntity;
import ai.classifai.db.entities.annotation.AnnotationEntity;
import ai.classifai.db.entities.data.ImageEntity;
import ai.classifai.db.handler.LabelHandler;
import ai.classifai.db.handler.ProjectHandler;
import ai.classifai.db.entities.ProjectEntity;
import ai.classifai.db.entities.annotation.AnnotationListFactory;
import ai.classifai.db.entities.data.DataEntity;
import ai.classifai.db.entities.dataVersion.DataVersionEntity;
import ai.classifai.db.repository.*;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.data.*;
import ai.classifai.util.exception.projectExistedException;
import ai.classifai.util.message.ErrorCodes;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.*;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * verticle that handles database
 *
 * @author YCCertifai
 */
@Slf4j
public class DatabaseVerticle extends AbstractVerticle implements VerticleServiceable
{
    private EntityManagerFactory entityManagerFactory;

    @Override
    public void onMessage(Message<JsonObject> message)
    {
        boolean isMessageValid = message.headers().contains(ParamConfig.getActionKeyword());

        if (!isMessageValid)
        {
            String errorMsg = "No action header specified for message";
            log.error(errorMsg);
            message.fail(ErrorCodes.NO_ACTION_SPECIFIED.ordinal(), errorMsg);
            return;
        }

        String action = message.headers().get(ParamConfig.getActionKeyword());

        switch (action) {
            case DbActionConfig.GET_ALL_PROJECT_META -> this.getAllProjectsMetadata(message);
            case DbActionConfig.CREATE_PROJECT -> this.createProject(message);
            case DbActionConfig.GET_PROJECT_META -> this.getProjectMetadata(message);
            case DbActionConfig.LOAD_PROJECT -> this.loadProject(message);
            case DbActionConfig.GET_THUMBNAIL -> this.getThumbnail(message);
            case DbActionConfig.GET_IMAGE_SOURCE -> this.getDataSource(message);
            case DbActionConfig.STAR_PROJECT -> this.starProject(message);
            case DbActionConfig.ADD_LABEL -> this.updateLabel(message);
            case DbActionConfig.DELETE_PROJECT -> this.deleteProject(message);
            case DbActionConfig.UPDATE_DATA -> this.updateData(message);

        //*******************************V2*******************************
            case DbActionConfig.RELOAD_PROJECT -> this.reloadProject(message);
            case DbActionConfig.CLOSE_PROJECT_STATE -> this.closeProjectState(message);
//        else if(action.equals(PortfolioDbQuery.getExportProject()))
//        {
//            this.exportProject(message);
//        }
            case DbActionConfig.RENAME_PROJECT -> this.renameProject(message);
            default -> {
                String msg = "Database query error. Action did not have an assigned function for handling.";
                log.error(msg);
                message.fail(ErrorCodes.NO_ACTION_SPECIFIED.ordinal(), msg);
            }
        }
    }

    private void closeProjectState(Message<JsonObject> message)
    {
        ProjectRepository repository = getProjectRepository();

        int annotationTypeIdx = getAnnotationTypeIdx(message);
        String projectName = getProjectName(message);

        Handler<Promise<ProjectEntity>> getProjectHandler = promise ->
                getProject(promise, projectName, annotationTypeIdx, repository);

        Function<ProjectEntity, Future<ProjectEntity>> closeProjectStateHandler = this::setProjectCloseState;

        Function<ProjectEntity, Future<Void>> persistProjectHandler = project ->
                vertx.executeBlocking(promise -> persistProject(promise, project, repository));

        vertx.executeBlocking(getProjectHandler)
                .compose(closeProjectStateHandler)
                .compose(persistProjectHandler)
                .onSuccess(unused -> message.replyAndRequest(ReplyHandler.getOkReply()))
                .onFailure(throwable -> message.replyAndRequest(ReplyHandler.reportUserDefinedError(throwable.getMessage())))
                .onComplete(unused -> Repository.closeRepositories(repository));
    }

    private Future<ProjectEntity> setProjectCloseState(ProjectEntity project) {
        Promise<ProjectEntity> promise = Promise.promise();
        ProjectEntity.LOADED_PROJECT_LIST.remove(project);
        promise.complete(project);
        return promise.future();
    }

    private void renameProject(Message<JsonObject> message)
    {
        ProjectRepository repository = getProjectRepository();

        int annotationTypeIdx = getAnnotationTypeIdx(message);
        String projectName = getProjectName(message);
        String newProjectName = getNewProjectName(message);

        Handler<Promise<ProjectEntity>> getProjectHandler = promise ->
                getProject(promise, projectName, annotationTypeIdx, repository);

        Function<ProjectEntity, Future<ProjectEntity>> renameProjectHandler = project ->
                vertx.executeBlocking(promise -> renameProject(promise, project, newProjectName));

        Function<ProjectEntity, Future<Void>> persistProjectHandler = project ->
                vertx.executeBlocking(promise -> persistProject(promise, project, repository));

        vertx.executeBlocking(getProjectHandler)
                .compose(renameProjectHandler)
                .compose(persistProjectHandler)
                .onSuccess(unused -> message.replyAndRequest(ReplyHandler.getOkReply()))
                .onFailure(throwable -> message.replyAndRequest(ReplyHandler.reportUserDefinedError(throwable.getMessage())))
                .onComplete(unused -> Repository.closeRepositories(repository));
    }

    private void persistProject(Promise<Void> promise, ProjectEntity project, ProjectRepository repository)
    {
        repository.saveProject(project);

        promise.complete();
    }

    private void renameProject(Promise<ProjectEntity> promise, ProjectEntity project, String newProjectName)
    {
        project.setName(newProjectName);
        promise.complete(project);
    }

    private void updateData(Message<JsonObject> message) {

        ProjectRepository projectRepository = getProjectRepository();
        DataVersionRepository dataVersionRepository = getDataVersionRepository();
        AnnotationRepository annotationRepository = getAnnotationRepository();

        int annotationTypeIdx = getAnnotationTypeIdx(message);
        String projectName = getProjectName(message);
        String uuid = getUuid(message);

        JsonObject request = message.body();

        Handler<Promise<ProjectEntity>> getProjectHandler = promise ->
                getProject(promise, projectName, annotationTypeIdx, projectRepository);

        Function<ProjectEntity, Future<DataEntity>> getDataHandler = project ->
                vertx.executeBlocking(promise -> getData(promise, project, projectName, annotationTypeIdx, uuid));

        Function<DataEntity, Future<Pair<List<AnnotationEntity>, DataVersionEntity>>> mergeDataVersionHandler = data ->
                vertx.executeBlocking(promise -> mergeAnnotationListPair(promise, data, request));

        Function<Pair<List<AnnotationEntity>, DataVersionEntity>, Future<Void>> persistDataVersion = pair ->
                vertx.executeBlocking(promise -> persistDataVersion(promise, pair, dataVersionRepository, annotationRepository));

        // getProject
        // getData
        // mergeDataVersion
        // persist
        vertx.executeBlocking(getProjectHandler)
                .compose(getDataHandler)
                .compose(mergeDataVersionHandler)
                .compose(persistDataVersion)
                .onSuccess(unused -> message.replyAndRequest(ReplyHandler.getOkReply()))
                .onFailure(throwable -> message.replyAndRequest(ReplyHandler.reportUserDefinedError(throwable.getMessage())))
                .onComplete(unused -> Repository.closeRepositories(projectRepository, dataVersionRepository, annotationRepository));
    }



    // FIXME: need extra api for delete label
//    private void updateLabel(Message<JsonObject> message) {
//        ProjectRepository projectRepository = getProjectRepository();
//        LabelRepository labelRepository = getLabelRepository();
//
//        int annotationTypeIdx = getAnnotationTypeIdx(message);
//        String name = getName(message);
//        List<String> strLabelList = getStrLabelList(message);
//
//        Handler<Promise<Project>> getProjectHandler = promise ->
//                getProject(promise, name, annotationTypeIdx, projectRepository);
//
//        Function<Project, Future<List<Label>>> mergeLabelListHandler = project ->
//                vertx.executeBlocking(promise -> mergeLabelList(promise, project, strLabelList));
//
//        Function<List<Label>, Future<Void>> persistLabelListHandler = labelList ->
//                vertx.executeBlocking(promise -> persistLabelList(promise, labelList, labelRepository));
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

    // temporary solution
    private void updateLabel(Message<JsonObject> message) {
        ProjectRepository projectRepository = getProjectRepository();
        LabelRepository labelRepository = getLabelRepository();

        int annotationTypeIdx = getAnnotationTypeIdx(message);
        String projectName = getProjectName(message);
        List<String> strLabelList = getStrLabelList(message);

        Handler<Promise<ProjectEntity>> getProjectHandler = promise ->
                getProject(promise, projectName, annotationTypeIdx, projectRepository);

        Function<ProjectEntity, Future<Pair<List<LabelEntity>, List<LabelEntity>>>> mergeLabelListHandler = project ->
                vertx.executeBlocking(promise -> mergeLabelList(promise, project, strLabelList));

        Function<Pair<List<LabelEntity>, List<LabelEntity>>, Future<Void>> persistLabelListHandler = pair ->
                vertx.executeBlocking(promise -> persistLabelListPair(promise, pair, labelRepository));

        // get project
        // merge label list
        // persist
        vertx.executeBlocking(getProjectHandler)
                .compose(mergeLabelListHandler)
                .compose(persistLabelListHandler)
                .onSuccess(unused -> message.replyAndRequest(ReplyHandler.getOkReply()))
                .onFailure(throwable -> message.replyAndRequest(ReplyHandler.reportUserDefinedError(throwable.getMessage())))
                .onComplete(unused -> Repository.closeRepositories(projectRepository, labelRepository));
    }

    // call load project
    private void reloadProject(Message<JsonObject> message) {
        ProjectRepository projectRepository = getProjectRepository();
        DataRepository dataRepository = getDataRepository();

        int annotationTypeIdx = getAnnotationTypeIdx(message);
        String projectName = getProjectName(message);

        Handler<Promise<ProjectEntity>> getProjectHandler = promise ->
                getProject(promise, projectName, annotationTypeIdx, projectRepository);

        Function<ProjectEntity, Future<List<DataEntity>>> mergeDataListHandler = project ->
                vertx.executeBlocking(promise -> getNewlyAddedDataList(promise, project));

        Function<List<DataEntity>, Future<List<DataEntity>>> persistDataListHandler = dataList ->
                vertx.executeBlocking(promise -> persistDataList(promise, dataList, dataRepository));

        Handler<List<DataEntity>> successHandler = dataList ->
        {
            JsonObject jsonObj = new JsonObject();
            jsonObj.put("uuid_add_list", DataHandler.getDataIdList(dataList));
            message.replyAndRequest(jsonObj);
        };

        // get project
        // merge dataList
        // persist
        vertx.executeBlocking(getProjectHandler)
                .compose(mergeDataListHandler)
                .compose(persistDataListHandler)
                .onSuccess(successHandler)
                .onFailure(throwable -> message.replyAndRequest(ReplyHandler.reportUserDefinedError(throwable.getMessage())))
                .onComplete(unused -> Repository.closeRepositories(projectRepository, dataRepository));
    }

    private void deleteProject(Message<JsonObject> message) {
        ProjectRepository projectRepository = getProjectRepository();

        int annotationTypeIdx = getAnnotationTypeIdx(message);
        String projectName = getProjectName(message);

        Handler<Promise<ProjectEntity>> getProjectHandler = promise -> getProject(promise, projectName, annotationTypeIdx, projectRepository);
        
        Function<ProjectEntity, Future<Void>> deleteProjectHandler = project ->
                vertx.executeBlocking(promise -> deleteProject(promise, project, projectRepository));

        vertx.executeBlocking(getProjectHandler)
                .compose(deleteProjectHandler)
                .onSuccess(unused -> message.replyAndRequest(ReplyHandler.getOkReply()))
                .onFailure(throwable -> message.replyAndRequest(ReplyHandler.reportUserDefinedError(throwable.getMessage())))
                .onComplete(unused -> Repository.closeRepositories(projectRepository));
    }

    private void starProject(Message<JsonObject> message) {
        ProjectRepository repository = getProjectRepository();

        boolean isStarred = getStarStatus(message);
        int annotationTypeIdx = getAnnotationTypeIdx(message);
        String projectName = getProjectName(message);

        Handler<Promise<ProjectEntity>> getProjectHandler = promise -> getProject(promise, projectName, annotationTypeIdx, repository);

        Function<ProjectEntity, Future<Void>> starProjectHandler = project ->
                vertx.executeBlocking(promise -> starProject(promise, project, isStarred, repository));

        vertx.executeBlocking(getProjectHandler)
                .compose(starProjectHandler)
                .onSuccess(unused -> message.replyAndRequest(ReplyHandler.getOkReply()))
                .onFailure(throwable -> message.replyAndRequest(ReplyHandler.reportDatabaseQueryError(throwable.getCause())))
                .onComplete(unused -> Repository.closeRepositories(repository));
    }


    private void getDataSource(Message<JsonObject> message) {
        ProjectRepository repository = getProjectRepository();

        int annotationTypeIdx = getAnnotationTypeIdx(message);
        String projectName = getProjectName(message);
        String uuid = getUuid(message);


        Handler<Promise<ProjectEntity>> getProjectHandler = promise -> getProject(promise, projectName, annotationTypeIdx, repository);

        Function<ProjectEntity, Future<DataEntity>> getDataHandler = project ->
                vertx.executeBlocking(promise -> getData(promise, project, projectName, annotationTypeIdx, uuid));

        Function<DataEntity, Future<String>> getImgSourceHandler = image ->
                vertx.executeBlocking(promise -> getDataSource(promise, image));

        Handler<String> successHandler = imgSource ->
        {
            JsonObject jsonObj = ReplyHandler.getOkReply();

            jsonObj.put("img_src", imgSource);

            message.replyAndRequest(jsonObj);
        };

        vertx.executeBlocking(getProjectHandler)
                .compose(getDataHandler)
                .compose(getImgSourceHandler) // can send to other verticles in future
                .onSuccess(successHandler)
                .onFailure(throwable -> message.replyAndRequest(ReplyHandler.reportUserDefinedError(throwable.getCause().getMessage())))
                .onComplete(unused -> Repository.closeRepositories(repository));
    }

    // FIXME: is this only for image
    private void getThumbnail(Message<JsonObject> message) {
        // store object due to reference passing throughout multiple composes
        final JsonObject STORE = new JsonObject();

        ProjectRepository repository = getProjectRepository();

        int annotationTypeIdx = getAnnotationTypeIdx(message);
        String projectName = getProjectName(message);
        String uuid = getUuid(message);

        Handler<Promise<ProjectEntity>> getProjectHandler = promise ->
                getProject(promise, projectName, annotationTypeIdx, repository);

        Function<ProjectEntity, Future<DataEntity>> getDataHandler = project ->
                vertx.executeBlocking(promise -> getData(promise, project, projectName, annotationTypeIdx, uuid));

        Function<Void, Future<String>> getThumbnailHandler = unused ->
                vertx.executeBlocking(promise -> getThumbnail(promise, (ImageEntity) STORE.getValue("image")));

        Handler<String> successHandler = thumbnail ->
        {
            JsonObject jsonObj = ReplyHandler.getOkReply();

            DataEntity dataEntity = (DataEntity) STORE.getValue("image");

            jsonObj.mergeIn(dataEntity.loadData());

            jsonObj.put("img_thumbnail", thumbnail);

            message.replyAndRequest(jsonObj);

        };

        vertx.executeBlocking(getProjectHandler)
                .compose(getDataHandler)
                .compose(img -> storeObject(STORE, "image", img))
                .compose(getThumbnailHandler) // can send to other verticles in future
                .onSuccess(successHandler)
                .onFailure(throwable -> message.replyAndRequest(ReplyHandler.reportUserDefinedError(throwable.getCause().getMessage())))
                .onComplete(unused -> Repository.closeRepositories(repository));
    }

    // FIXME: perform image validation here!!!!!
    private void loadProject(Message<JsonObject> message) {
        ProjectRepository repository = getProjectRepository();

        int annotationTypeIdx = getAnnotationTypeIdx(message);
        String projectName = getProjectName(message);

        Handler<Promise<ProjectEntity>> getProjectHandler = promise ->
                getProject(promise, projectName, annotationTypeIdx, repository);

        Handler<ProjectEntity> successHandler = project ->
        {
            JsonObject jsonObj = ReplyHandler.getOkReply();

//            jsonObj.put("label_list", LabelHandler.getStringListFromLabelList(project.getCurrentVersionEntity().getLabelList()));

            List<String> uuidList = project.getDataList().stream()
                    .map(data -> data.getDataId().toString())
                    .collect(Collectors.toList());

            jsonObj.put("uuid_list", uuidList);

//            ProjectEntity.LOADED_PROJECT_LIST.add(project);

            message.replyAndRequest(jsonObj);
        };

        vertx.executeBlocking(getProjectHandler)
                .onSuccess(successHandler)
                .onFailure(throwable -> message.replyAndRequest(ReplyHandler.reportDatabaseQueryError(throwable.getCause())))
                .onComplete(unused -> Repository.closeRepositories(repository));
    }

    private void getProjectMetadata(Message<JsonObject> message)
    {
        ProjectRepository repository = getProjectRepository();

        int annotationTypeIndex = getAnnotationTypeIdx(message);
        String projectName = getProjectName(message);

        Handler<Promise<ProjectEntity>> getProjectHandler = promise -> getProject(promise, projectName, annotationTypeIndex, repository);

        Handler<ProjectEntity> successHandler = project ->
        {
            JsonObject jsonObj = ReplyHandler.getOkReply();

//            JsonObject projectMeta = project.getProjectMeta();
            List<JsonObject> projectMetaList = new ArrayList<>();
//            projectMetaList.add(projectMeta);

            jsonObj.put("content", projectMetaList); //TODO: hardcoded key

            message.replyAndRequest(jsonObj);
        };

        vertx.executeBlocking(getProjectHandler)
                .onSuccess(successHandler)
                .onFailure(throwable -> message.replyAndRequest(ReplyHandler.reportDatabaseQueryError(throwable.getCause())))
                .onComplete(unused -> Repository.closeRepositories(repository));
    }

    private void createProject(Message<JsonObject> message)
    {
        ProjectRepository projectRepository = getProjectRepository();

        JsonObject msgBody = message.body();

        int annotationTypeIdx = getAnnotationTypeIdx(message);
        String annotation = AnnotationType.fromInt(annotationTypeIdx).name();

        String projectName = getProjectName(message);

        String projectPath = msgBody.getString(ParamConfig.getProjectPathParam());

        String labelPath = msgBody.getString(ParamConfig.getLabelPathParam());

        Handler<Promise<ProjectEntity>> createProject = promise ->
        {
            try
            {
                // check if data is repeated
                if (projectRepository.getProjectByNameAndAnnotation(projectName, annotationTypeIdx) != null)
                {
                    String errorMsg = String.format("[%s] %s project existed", annotation, projectName);
                    log.error(errorMsg);
                    throw new projectExistedException(errorMsg);
                }

                // create new project
                ProjectEntity project = ProjectHandler.buildNewProject(projectName, annotationTypeIdx, projectPath, labelPath);

                projectRepository.saveProject(project);

                promise.complete(project);
            }
            catch (Exception e)
            {
                log.error(e.getMessage());
                promise.fail(e);
            }
        };

        Handler<ProjectEntity> successHandler = project ->
        {
            JsonObject jsonObj = ReplyHandler.getOkReply();

            List<JsonObject> metaList = new ArrayList<>();
            metaList.add(project.getProjectMeta());

            jsonObj.put("content", metaList);

            message.replyAndRequest(jsonObj);
        };

        vertx.executeBlocking(createProject)
                .onSuccess(successHandler)
                .onFailure(throwable -> message.replyAndRequest(ReplyHandler.reportDatabaseQueryError(throwable.getCause())))
                .onComplete(unused -> Repository.closeRepositories(projectRepository));

    }

    private void getAllProjectsMetadata(Message<JsonObject> message)
    {
        int annotationTypeIdx = getAnnotationTypeIdx(message);
        ProjectRepository repository = getProjectRepository();

        Handler<Promise<List<ProjectEntity>>> getAllProjectMeta = promise ->
        {
            List<ProjectEntity> projectList = repository.getProjectListByAnnotation(annotationTypeIdx);

            promise.complete(projectList);
        };

        Handler<List<ProjectEntity>> successHandler = projectList ->
        {
            JsonObject jsonObj = ReplyHandler.getOkReply();

            List<JsonObject> metaList = projectList.stream()
                    .map(ProjectEntity::getProjectMeta)
                    .collect(Collectors.toList());

            jsonObj.put("content", metaList); //TODO: hardcoded key

            message.replyAndRequest(jsonObj);
        };

        vertx.executeBlocking(getAllProjectMeta)
                .onSuccess(successHandler)
                .onFailure(throwable -> message.replyAndRequest(ReplyHandler.reportDatabaseQueryError(throwable.getCause())))
                .onComplete(unused -> Repository.closeRepositories(repository));
    }

    private void mergeAnnotationListPair(Promise<Pair<List<AnnotationEntity>, DataVersionEntity>> promise, DataEntity dataEntity, JsonObject request)
    {
        DataVersionEntity dataVersionEntity = dataEntity.getCurrentDataVersion();

        dataVersionEntity.updateDataFromJson(request);

        AnnotationListFactory annotationListFactory = new AnnotationListFactory();

        List<AnnotationEntity> annotationEntityList = annotationListFactory.getAnnotationListFromJson(request, dataVersionEntity);

        AnnotationHandler annotationHandler = new AnnotationHandler();
        List<AnnotationEntity> deleteList = annotationHandler.getDeleteList(annotationEntityList, dataVersionEntity.getAnnotationEntities());

        dataVersionEntity.setAnnotationEntities(annotationEntityList);

        promise.complete(new ImmutablePair<>(deleteList, dataVersionEntity));
    }

    private void persistDataVersion(Promise<Void> promise, Pair<List<AnnotationEntity>, DataVersionEntity> annotationListPair,
                                    DataVersionRepository repository, AnnotationRepository annotationRepository)
    {
        DataVersionEntity dataVersionEntity = annotationListPair.getRight();

        annotationRepository.removeAnnotationList(annotationListPair.getLeft());

        repository.saveDataVersion(dataVersionEntity);

        promise.complete();
    }

//    private void mergeDataVersion(Promise<DataVersion> promise, Data data, JsonObject request)
//    {
//        DataVersion dataVersion = data.getCurrentDataVersion();
//
//        dataVersion.updateDataFromJson(request);
//
//        promise.complete(dataVersion);
//    }
//
//    private void persistDataVersion(Promise<Void> promise, DataVersion dataVersion, DataVersionRepository repository)
//    {
//        repository.saveDataVersion(dataVersion);
//
//        promise.complete();
//    }

//    private void mergeLabelList(Promise<List<Label>> promise, Project project, List<String> strLabelList)
//    {
//        LabelHandler labelHandler = new LabelHandler();
//
//        List<Label> labelList = labelHandler.getLabelList(project, strLabelList);
//
//        promise.complete(labelList);
//    }

    // left: to delete, right: to udpate
    private void mergeLabelList(Promise<Pair<List<LabelEntity>, List<LabelEntity>>> promise, ProjectEntity project, List<String> strLabelList)
    {
        LabelHandler labelHandler = new LabelHandler();

        List<LabelEntity> deleteList = labelHandler.getDeleteList(project, strLabelList);
        List<LabelEntity> labelEntityList = labelHandler.getLabelList(project, strLabelList);

        promise.complete(new ImmutablePair<>(deleteList, labelEntityList));
    }

    private void persistLabelListPair(Promise<Void> promise, Pair<List<LabelEntity>, List<LabelEntity>> pair, LabelRepository repository)
    {
        repository.removeLabelList(pair.getLeft());

        repository.saveLabelList(pair.getRight());

        promise.complete();
    }

    private void persistLabelList(Promise<Void> promise, List<LabelEntity> labelEntityList, LabelRepository repository)
    {
        repository.saveLabelList(labelEntityList);

        promise.complete();
    }

    private void persistDataList(Promise<List<DataEntity>> promise, List<DataEntity> dataEntityList, DataRepository repository)
    {
        repository.saveDataList(dataEntityList);

        promise.complete(dataEntityList);
    }

    private void getNewlyAddedDataList(Promise<List<DataEntity>> promise, ProjectEntity project)
    {
        DataHandler dataHandler = DataHandler.getDataHandler(project.getAnnotationType());

        if (dataHandler == null)
        {
            promise.fail(String.format("Unable to identify annotation type for project %s", project.getName()));
            return;
        }

        List<DataEntity> dataEntityList = dataHandler.getNewlyAddedDataList(project);

        promise.complete(dataEntityList);
    }

    private void getDataSource(Promise<String> promise, DataEntity dataEntity)
    {
        DataHandler dataHandler = DataHandler.getDataHandler(dataEntity.getProject().getAnnotationType());
        if (dataHandler == null)
        {
            promise.fail(String.format("Unable to identify annotation type for project %s", dataEntity.getProject().getName()));
            return;
        }

        String dataSource = dataHandler.generateDataSource(dataEntity);

        if (dataSource.length() == 0)
        {
            ProjectEntity project = dataEntity.getProject();

            promise.fail(String.format("Failed to generate image source for data %s in project: [%s] %s",
                    dataEntity.getFullPath(), AnnotationType.fromInt(project.getAnnotationType()).name(),
                    project.getName()));
            return;
        }

        promise.complete(dataSource);
    }


    // TODO: required further thought on API writing
    private void getThumbnail(Promise<String> promise, ImageEntity imageEntity)
    {
        String thumbnail = new ImageHandler().generateThumbnail(imageEntity);

        if (thumbnail.length() == 0)
        {
            ProjectEntity project = imageEntity.getProject();

            promise.fail(String.format("Failed to generate thumbnail for data %s in project: [%s] %s",
                    imageEntity.getFullPath(), AnnotationType.fromInt(project.getAnnotationType()).name(),
                    project.getName()));
            return;
        }

        promise.complete(thumbnail);
    }

    private void deleteProject(Promise<Void> promise, ProjectEntity project, ProjectRepository projectRepository)
    {
        projectRepository.deleteProject(project);
        promise.complete();

    }

    private void getData(Promise<DataEntity> promise, ProjectEntity project, String projectName, Integer annotationTypeIdx, String uuid)
    {
        Optional<DataEntity> data = project.getDataList().stream()
                .filter(d -> d.withId(uuid))
                .findFirst();

        if (data.isEmpty())
        {
            promise.fail(String.format("No entity found for data %s in project: [%s] %s",
                    uuid, AnnotationType.values()[annotationTypeIdx], projectName));
            return;
        }

        promise.complete(data.get());
    }

    private void starProject(Promise<Void> promise, ProjectEntity project, boolean isStarred, ProjectRepository repository) {
        project.setStarred(isStarred);

        repository.saveProject(project);

        promise.complete();
    }

    private void getProject(Promise<ProjectEntity> promise, String projectName, int annotationTypeIdx, ProjectRepository repository)
    {
        ProjectEntity project = repository.getProjectByNameAndAnnotation(projectName, annotationTypeIdx);

        if (project == null)
        {
            promise.fail(String.format("No entity found for project: [%s] %s",
                    AnnotationType.values()[annotationTypeIdx], projectName));
            return;
        }

        promise.complete(project);
    }

    private Future<Void> storeObject(JsonObject store, String key, Object obj)
    {
        store.put(key, obj);
        return Future.succeededFuture();
    }

    private List<String> getStrLabelList(Message<JsonObject> message)
    {
        return StringHandler.stringToStringList(message.body().getString(ParamConfig.getLabelListParam()));
    }

    private boolean getStarStatus(Message<JsonObject> message)
    {
        return Boolean.parseBoolean(message.body().getString(ParamConfig.getStatusParam()));
    }

    private String getUuid(Message<JsonObject> message)
    {
        return message.body().getString(ParamConfig.getUuidParam());
    }

    private String getNewProjectName(Message<JsonObject> message)
    {
        return message.body().getString(ParamConfig.getNewProjectNameParam());
    }

    private String getProjectName(Message<JsonObject> message)
    {
        return message.body().getString(ParamConfig.getProjectNameParam());
    }

    private int getAnnotationTypeIdx(Message<JsonObject> message)
    {
        return message.body().getInteger(ParamConfig.getAnnotationTypeParam());
    }

    private LabelRepository getLabelRepository() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        return new LabelRepository(entityManager);
    }

    private AnnotationRepository getAnnotationRepository() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        return new AnnotationRepository(entityManager);
    }

    private DataRepository getDataRepository()
    {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        return new DataRepository(entityManager);
    }

    private ProjectRepository getProjectRepository()
    {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        return new ProjectRepository(entityManager);
    }

    private DataVersionRepository getDataVersionRepository()
    {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        return new DataVersionRepository(entityManager);
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception
    {
        vertx.executeBlocking(this::connectDatabase)
                .onSuccess(r -> successHandler(startPromise))
                .onFailure(r -> failureHandler(startPromise, r));
    }

    private void successHandler(Promise<Void> promise)
    {
        vertx.eventBus().consumer(DbActionConfig.QUEUE, this::onMessage);
        promise.complete();
    }

    private void failureHandler(Promise<Void> promise, Throwable e)
    {
        e.printStackTrace();
        log.error(String.format("Database preparation error%n%s", e.getMessage()));
        promise.fail(e.getCause());
    }

    private void connectDatabase(Promise<Void> promise)
    {
        try
        {
            entityManagerFactory = Persistence.createEntityManagerFactory("database");
        }
        catch (Exception e)
        {
            promise.fail(e);
        }

        promise.complete();
    }
}
