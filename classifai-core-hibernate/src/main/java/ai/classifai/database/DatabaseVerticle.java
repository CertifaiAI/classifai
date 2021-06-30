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

import ai.classifai.action.LabelListImport;
import ai.classifai.database.model.Label;
import ai.classifai.database.model.Project;
import ai.classifai.database.model.data.Data;
import ai.classifai.database.repository.ProjectRepository;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.data.DataHandler;
import ai.classifai.util.data.DataHandlerFactory;
import ai.classifai.util.exception.projectExistedException;
import ai.classifai.util.message.ErrorCodes;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.type.AnnotationHandler;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
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
//        else if (action.equals(PortfolioDbQuery.getUpdateLabelList()))
//        {
//            this.updateLabelList(message);
//        }
//        else if (action.equals(PortfolioDbQuery.getDeleteProject()))
//        {
//            this.deleteProject(message);
//        }
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
//        else
//        {
//            log.error("Portfolio query error. Action did not have an assigned function for handling.");
//        }

    }

    private void getProjectMetadata(Message<JsonObject> message)
    {
        Integer annotationTypeIndex = message.body().getInteger(ParamConfig.getAnnotationTypeParam());

        String projectName = message.body().getString(ParamConfig.getProjectNameParam());

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        Handler<Promise<Project>> getProjectMeta = promise ->
        {
            ProjectRepository projectRepository = new ProjectRepository(entityManager);
            Project project = projectRepository.getProjectByNameAndAnnotation(projectName, annotationTypeIndex);

            if (project == null) promise.fail(String.format("No entity found for project: [%s] %s",
                    AnnotationType.values()[annotationTypeIndex], projectName));


        };

        Handler<AsyncResult<Project>> resultHandler = result ->
        {
            if (result.succeeded())
            {
                JsonObject jsonObj = ReplyHandler.getOkReply();

                JsonObject projectMeta = result.result().getProjectMeta();

                jsonObj.put("content", projectMeta); //TODO: hardcoded key

                message.replyAndRequest(jsonObj);
            }
            else
            {
                message.replyAndRequest(ReplyHandler.reportDatabaseQueryError(result.cause()));
            }
        };

        vertx.executeBlocking(getProjectMeta, resultHandler);

    }

    private void createProject(Message<JsonObject> message)
    {
        JsonObject msgBody = message.body();

        String annotation = msgBody.getString(ParamConfig.getAnnotationTypeParam());
        Integer annotationTypeIndex = AnnotationHandler.getType(annotation).ordinal();

        String projectName = msgBody.getString(ParamConfig.getProjectNameParam());

        String projectPath = msgBody.getString(ParamConfig.getProjectPathParam());

        String labelPath = msgBody.getString(ParamConfig.getLabelPathParam());

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        Handler<Promise<Project>> createProject = promise ->
        {
            try
            {
                ProjectRepository projectRepository = new ProjectRepository(entityManager);

                // check if data is repeated
                if (projectRepository.getProjectByNameAndAnnotation(projectName, annotationTypeIndex) != null)
                {
                    throw new projectExistedException(String.format("[%s] %s project existed", annotation, projectName));
                }

                // create new project
                Project project = Project.buildNewProject(projectName, annotationTypeIndex, projectPath, labelPath);

                projectRepository.createNewProject(project);

                promise.complete(project);
            }
            catch (Exception e)
            {
                log.error(e.getMessage());
                promise.fail(e);
            }
        };

        Handler<AsyncResult<Project>> resultHandler = result ->
        {
            if (result.succeeded())
            {
                JsonObject jsonObj = ReplyHandler.getOkReply();

                List<JsonObject> metaList = new ArrayList<>();
                metaList.add(result.result().getProjectMeta());

                jsonObj.put("content", metaList);

                message.replyAndRequest(jsonObj);
            }
            else
            {
                message.replyAndRequest(ReplyHandler.reportDatabaseQueryError(result.cause()));
            }
        };

        vertx.executeBlocking(createProject, resultHandler);

    }

    private void getAllProjectsMetadata(Message<JsonObject> message)
    {
        Integer annotationTypeIndex = message.body().getInteger(ParamConfig.getAnnotationTypeParam());

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        Handler<Promise<List<Project>>> getAllProjectMeta = promise ->
        {
            ProjectRepository projectRepository = new ProjectRepository(entityManager);
            List<Project> projectList = projectRepository.getProjectListByAnnotation(annotationTypeIndex);

            promise.complete(projectList);
        };

        Handler<AsyncResult<List<Project>>> resultHandler = result ->
        {
            if (result.succeeded())
            {
                JsonObject jsonObj = ReplyHandler.getOkReply();

                List<JsonObject> metaList = result.result().stream()
                        .map(Project::getProjectMeta)
                        .collect(Collectors.toList());

                jsonObj.put("content", metaList); //TODO: hardcoded key

                message.replyAndRequest(jsonObj);
            }
            else
            {
                message.replyAndRequest(ReplyHandler.reportDatabaseQueryError(result.cause()));
            }
        };

        vertx.executeBlocking(getAllProjectMeta, resultHandler);
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
