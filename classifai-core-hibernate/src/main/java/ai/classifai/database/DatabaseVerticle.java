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

import ai.classifai.database.model.Project;
import ai.classifai.database.model.data.Data;
import ai.classifai.database.model.data.Image;
import ai.classifai.database.model.dataVersion.DataVersion;
import ai.classifai.database.repository.DataRepository;
import ai.classifai.database.repository.DataVersionRepository;
import ai.classifai.database.repository.ProjectRepository;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.data.ImageHandler;
import ai.classifai.util.exception.projectExistedException;
import ai.classifai.util.message.ErrorCodes;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.type.AnnotationHandler;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.*;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * verticle that handles database
 *
 * @author codenamewei
 */
@Slf4j
public class DatabaseVerticle extends AbstractVerticle implements VerticleServiceable
{
    private EntityManagerFactory entityManagerFactory;

    @Override
    public void onMessage(Message<JsonObject> message)
    {
        // FIXME:
        //  Too many duplicate code
        if (!message.headers().contains(ParamConfig.getActionKeyword()))
        {
            log.error("No action header specified for message with headers {} and body {}",
                    message.headers(), message.body().encodePrettily());

            message.fail(ErrorCodes.NO_ACTION_SPECIFIED.ordinal(), "No keyword " + ParamConfig.getActionKeyword() + " specified");

            return;
        }

        String action = message.headers().get(ParamConfig.getActionKeyword());

        // FIXME:
        //  Hardcoded
        if (action.equals(DbActionConfig.GET_ALL_PROJECT_META))
        {
            this.getAllProjectsMetadata(message);
        }
        else if (action.equals(DbActionConfig.CREATE_PROJECT))
        {
            this.createProject(message);
        }
        else if (action.equals(DbActionConfig.GET_PROJECT_META))
        {
            this.getProjectMetadata(message);
        }
        else if (action.equals(DbActionConfig.LOAD_PROJECT))
        {
            this.loadProject(message);
        }
        else if (action.equals(DbActionConfig.GET_THUMBNAIL))
        {
            this.getThumbnail(message);
        }
        else if (action.equals(DbActionConfig.GET_IMAGE_SOURCE))
        {
            this.getImageSource(message);
        }
        else if (action.equals(DbActionConfig.STAR_PROJECT))
        {
            this.starProject(message);
        }

//        else if (action.equals(PortfolioDbQuery.getUpdateLabelList()))
//        {
//            this.updateLabelList(message);
//        }
        else if (action.equals(DbActionConfig.DELETE_PROJECT))
        {
            this.deleteProject(message);
        }
//        //*******************************V2*******************************
//
//        else if (action.equals(PortfolioDbQuery.getRetrieveProjectMetadata()))
//        {
//            this.getProjectMetadata(message);
//        }
//        else if (action.equals(DbActionConfig.getGetAllProjectMeta()))
//        {
//            this.getAllProjectsMetadata(message);
//        }
//        else if (action.equals(PortfolioDbQuery.getStarProject()))
//        {
//            this.starProject(message);
//        }
//        else if(action.equals(PortfolioDbQuery.getReloadProject()))
//        {
//            this.reloadProject(message);
//        }
//        else if(action.equals(PortfolioDbQuery.getExportProject()))
//        {
//            this.exportProject(message);
//        }
//        else if(action.equals(PortfolioDbQuery.getRenameProject()))
//        {
//            renameProject(message);
//        }
        else
        {
            String msg = "Database query error. Action did not have an assigned function for handling.";
            log.error(msg);
            message.fail(ErrorCodes.NO_ACTION_SPECIFIED.ordinal(), msg);
        }
    }

    private void deleteProject(Message<JsonObject> message) {
        // store object due to reference passing throughout multiple composes
        final JsonObject STORE = new JsonObject();
        
        ProjectRepository projectRepository = getProjectRepository();
        DataVersionRepository dataVersionRepository = getDataVersionRepository();

        int annotationTypeIdx = getAnnotationTypeIdx(message);
        String projectName = getProjectName(message);

        Handler<Promise<Project>> getProjectHandler = promise -> getProject(promise, projectName, annotationTypeIdx, projectRepository);

        Function<Project, Future<List<Data>>> getDataListHandler = project ->
                vertx.executeBlocking(promise -> getDataList(promise, project));
        
        Function<Project, Future<List<DataVersion>>> getDataVersionListHandler = dataList ->
                vertx.executeBlocking(promise -> getDataVersionList(promise, dataList));
        
        Function<Void, Future<Void>> deleteProjectHandler = unused -> 
                vertx.executeBlocking(promise -> deleteProject(STORE, projectRepository, dataVersionRepository));

        vertx.executeBlocking(getProjectHandler)
                .compose(getDataListHandler)
                .compose(getDataVersionListHandler)
                .onSuccess()
                .onFailure()
                .onComplete();
    }

    private void deleteProject(JsonObject store, ProjectRepository projectRepository, DataVersionRepository dataVersionRepository) {
    }

    private void getDataVersionList(Promise<List<DataVersion>> promise, Project dataList) {
    }

    private void getDataList(Promise<List<Data>> promise, Project project) {
    }

    private void starProject(Message<JsonObject> message) {
        ProjectRepository repository = getProjectRepository();

        boolean isStarred = getStarStatus(message);
        int annotationTypeIdx = getAnnotationTypeIdx(message);
        String projectName = getProjectName(message);

        Handler<Promise<Project>> getProjectHandler = promise -> getProject(promise, projectName, annotationTypeIdx, repository);

        Function<Project, Future<Void>> starProjectHandler = project ->
                vertx.executeBlocking(promise -> starProject(promise, project, isStarred, repository));

        vertx.executeBlocking(getProjectHandler)
                .compose(starProjectHandler)
                .onSuccess(unused -> message.replyAndRequest(ReplyHandler.getOkReply()))
                .onFailure(throwable -> message.replyAndRequest(ReplyHandler.reportDatabaseQueryError(throwable.getCause())))
                .onComplete(unused -> repository.closeEntityManager());
    }


    private void getImageSource(Message<JsonObject> message) {
        ProjectRepository repository = getProjectRepository();

        int annotationTypeIdx = getAnnotationTypeIdx(message);
        String projectName = getProjectName(message);
        String uuid = getUuid(message);


        Handler<Promise<Project>> getProjectHandler = promise -> getProject(promise, projectName, annotationTypeIdx, repository);

        Function<Project, Future<Image>> getDataHandler = project ->
                vertx.executeBlocking(promise -> getData(promise, project, projectName, annotationTypeIdx, uuid));

        Function<Image, Future<String>> getImgSourceHandler = image ->
                vertx.executeBlocking(promise -> getImageSource(promise, image));

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
                .onComplete(unused -> repository.closeEntityManager());
    }

    // FIXME: is this only for image
    private void getThumbnail(Message<JsonObject> message) {
        // store object due to reference passing throughout multiple composes
        final JsonObject STORE = new JsonObject();

        ProjectRepository repository = getProjectRepository();

        int annotationTypeIdx = getAnnotationTypeIdx(message);
        String projectName = getProjectName(message);
        String uuid = getUuid(message);

        Handler<Promise<Project>> getProjectHandler = promise ->
                getProject(promise, projectName, annotationTypeIdx, repository);

        Function<Project, Future<Image>> getDataHandler = project ->
                vertx.executeBlocking(promise -> getData(promise, project, projectName, annotationTypeIdx, uuid));

        Function<Void, Future<String>> getThumbnailHandler = unused ->
                vertx.executeBlocking(promise -> getThumbnail(promise, (Image) STORE.getValue("image")));

        Handler<String> successHandler = thumbnail ->
        {
            JsonObject jsonObj = ReplyHandler.getOkReply();

            jsonObj.mergeIn(((Image) STORE.getValue("image")).loadImage(thumbnail));

            message.replyAndRequest(jsonObj);

        };

        vertx.executeBlocking(getProjectHandler)
                .compose(getDataHandler)
                .compose(img -> storeObject(STORE, "image", img))
                .compose(getThumbnailHandler) // can send to other verticles in future
                .onSuccess(successHandler)
                .onFailure(throwable -> message.replyAndRequest(ReplyHandler.reportUserDefinedError(throwable.getCause().getMessage())))
                .onComplete(unused -> repository.closeEntityManager());
    }

    // FIXME: perform image validation here!!!!!
    private void loadProject(Message<JsonObject> message) {
        ProjectRepository repository = getProjectRepository();

        int annotationTypeIdx = getAnnotationTypeIdx(message);
        String projectName = getProjectName(message);

        Handler<Promise<Project>> getProjectHandler = promise ->
                getProject(promise, projectName, annotationTypeIdx, repository);

        Handler<Project> successHandler = project ->
        {
            JsonObject jsonObj = ReplyHandler.getOkReply();

            jsonObj.put("label_list", project.getCurrentVersion().getLabelList());

            List<String> uuidList = project.getDataList().stream()
                    .map(data -> data.getDataId().toString())
                    .collect(Collectors.toList());

            jsonObj.put("uuid_list", uuidList);

            message.replyAndRequest(jsonObj);
        };

        vertx.executeBlocking(getProjectHandler)
                .onSuccess(successHandler)
                .onFailure(throwable -> message.replyAndRequest(ReplyHandler.reportDatabaseQueryError(throwable.getCause())))
                .onComplete(unused -> repository.closeEntityManager());
    }

    private void getProjectMetadata(Message<JsonObject> message)
    {
        ProjectRepository repository = getProjectRepository();

        int annotationTypeIndex = getAnnotationTypeIdx(message);
        String projectName = getProjectName(message);

        Handler<Promise<Project>> getProjectHandler = promise -> getProject(promise, projectName, annotationTypeIndex, repository);

        Handler<Project> successHandler = project ->
        {
            JsonObject jsonObj = ReplyHandler.getOkReply();

            JsonObject projectMeta = project.getProjectMeta();
            List<JsonObject> projectMetaList = new ArrayList<>();
            projectMetaList.add(projectMeta);

            jsonObj.put("content", projectMetaList); //TODO: hardcoded key

            message.replyAndRequest(jsonObj);
        };

        vertx.executeBlocking(getProjectHandler)
                .onSuccess(successHandler)
                .onFailure(throwable -> message.replyAndRequest(ReplyHandler.reportDatabaseQueryError(throwable.getCause())))
                .onComplete(unused -> repository.closeEntityManager());
    }

    private void createProject(Message<JsonObject> message)
    {
        ProjectRepository projectRepository = getProjectRepository();
        DataVersionRepository dataVersionRepository = getDataVersionRepository();

        JsonObject msgBody = message.body();

        int annotationTypeIdx = getAnnotationTypeIdx(message);
        String annotation = AnnotationType.fromInt(annotationTypeIdx).name();

        String projectName = getProjectName(message);

        String projectPath = msgBody.getString(ParamConfig.getProjectPathParam());

        String labelPath = msgBody.getString(ParamConfig.getLabelPathParam());

        Handler<Promise<Project>> createProject = promise ->
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
                Project project = Project.buildNewProject(projectName, annotationTypeIdx, projectPath, labelPath);

                projectRepository.saveProject(project);

                List<DataVersion> dataVersionList = project.getDataList()
                        .stream()
                        .map(Data::getDataVersions)
                        .flatMap(List::stream)
                        .collect(Collectors.toList());

                dataVersionRepository.saveDataVersionList(dataVersionList);

                promise.complete(project);
            }
            catch (Exception e)
            {
                log.error(e.getMessage());
                promise.fail(e);
            }
        };

        Handler<Project> successHandler = project ->
        {
            JsonObject jsonObj = ReplyHandler.getOkReply();

            List<JsonObject> metaList = new ArrayList<>();
            metaList.add(project.getProjectMeta());

            jsonObj.put("content", metaList);

            message.replyAndRequest(jsonObj);
        };

        Handler<AsyncResult<Project>> closeRepositoryHandler = unused ->
        {
            projectRepository.closeEntityManager();
            dataVersionRepository.closeEntityManager();
        };

        vertx.executeBlocking(createProject)
                .onSuccess(successHandler)
                .onFailure(throwable -> message.replyAndRequest(ReplyHandler.reportDatabaseQueryError(throwable.getCause())))
                .onComplete(closeRepositoryHandler);

    }

    private void getAllProjectsMetadata(Message<JsonObject> message)
    {
        int annotationTypeIdx = getAnnotationTypeIdx(message);
        ProjectRepository repository = getProjectRepository();

        Handler<Promise<List<Project>>> getAllProjectMeta = promise ->
        {
            List<Project> projectList = repository.getProjectListByAnnotation(annotationTypeIdx);

            promise.complete(projectList);
        };

        Handler<List<Project>> successHandler = projectList ->
        {
            JsonObject jsonObj = ReplyHandler.getOkReply();

            List<JsonObject> metaList = projectList.stream()
                    .map(Project::getProjectMeta)
                    .collect(Collectors.toList());

            jsonObj.put("content", metaList); //TODO: hardcoded key

            message.replyAndRequest(jsonObj);
        };

        vertx.executeBlocking(getAllProjectMeta)
                .onSuccess(successHandler)
                .onFailure(throwable -> message.replyAndRequest(ReplyHandler.reportDatabaseQueryError(throwable.getCause())))
                .onComplete(unused -> repository.closeEntityManager());
    }

    private void getImageSource(Promise<String> promise, Image image)
    {
        String imgSource = new ImageHandler().generateImageSource(image);

        if (imgSource.length() == 0)
        {
            Project project = image.getProject();

            promise.fail(String.format("Failed to generate thumbnail for data %s in project: [%s] %s",
                    image.getFullPath(), AnnotationType.fromInt(project.getAnnoType()).name(),
                    project.getProjectName()));
            return;
        }

        promise.complete(imgSource);
    }


    // TODO: required further thought on API writing
    private void getThumbnail(Promise<String> promise, Image image)
    {
        String thumbnail = new ImageHandler().generateThumbnail(image);

        if (thumbnail.length() == 0)
        {
            Project project = image.getProject();

            promise.fail(String.format("Failed to generate thumbnail for data %s in project: [%s] %s",
                    image.getFullPath(), AnnotationType.fromInt(project.getAnnoType()).name(),
                    project.getProjectName()));
            return;
        }

        promise.complete(thumbnail);
    }

    // TODO: required further thought on API writing
    //  think on how to refactor with other data type
    private void getData(Promise<Image> promise, Project project, String projectName, Integer annotationTypeIdx, String uuid)
    {
        Optional<Data> data = project.getDataList().stream()
                .filter(d -> d.withId(uuid))
                .findFirst();

        if (data.isEmpty())
        {
            promise.fail(String.format("No entity found for data %s in project: [%s] %s",
                    uuid, AnnotationType.values()[annotationTypeIdx], projectName));
            return;
        }

        promise.complete((Image) data.get());
    }

    private void starProject(Promise<Void> promise, Project project, boolean isStarred, ProjectRepository repository) {
        project.setStarred(isStarred);

        repository.saveProject(project);

        promise.complete();
    }

    private void getProject(Promise<Project> promise, String projectName, int annotationTypeIdx, ProjectRepository repository)
    {
        Project project = repository.getProjectByNameAndAnnotation(projectName, annotationTypeIdx);

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

    private boolean getStarStatus(Message<JsonObject> message)
    {
        return Boolean.parseBoolean(message.body().getString(ParamConfig.getStatusParam()));
    }

    private String getUuid(Message<JsonObject> message)
    {
        return message.body().getString(ParamConfig.getUuidParam());
    }

    private String getProjectName(Message<JsonObject> message)
    {
        return message.body().getString(ParamConfig.getProjectNameParam());
    }

    // FIXME: create confusion in developer
    //   define clearly when to use which data type
    private int getAnnotationTypeIdx(Message<JsonObject> message)
    {
        return message.body().getInteger(ParamConfig.getAnnotationTypeParam());
    }

    private DataRepository getDataRepository()
    {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        return new DataRepository(entityManager);
    }

    private DataVersionRepository getDataVersionRepository()
    {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        return new DataVersionRepository(entityManager);
    }

    private ProjectRepository getProjectRepository()
    {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        return new ProjectRepository(entityManager);
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception
    {
        System.out.println("do something \n\n\n\n\n\n\n\n\n\n");
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
        log.error("Database preparation error", e.getCause());
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
