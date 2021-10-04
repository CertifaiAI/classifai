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
package ai.classifai.database.portfolio;

import ai.classifai.action.ActionOps;
import ai.classifai.action.parser.PortfolioParser;
import ai.classifai.action.parser.ProjectParser;
import ai.classifai.database.DBUtils;
import ai.classifai.database.DbConfig;
import ai.classifai.database.VerticleServiceable;
import ai.classifai.database.annotation.AnnotationVerticle;
import ai.classifai.database.versioning.Annotation;
import ai.classifai.database.versioning.ProjectVersion;
import ai.classifai.database.versioning.Version;
import ai.classifai.dto.data.DataInfoProperties;
import ai.classifai.dto.data.ProjectMetaProperties;
import ai.classifai.dto.data.ThumbnailProperties;
import ai.classifai.loader.NameGenerator;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.loader.ProjectLoaderStatus;
import ai.classifai.selector.project.ProjectImportSelector;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.collection.UuidGenerator;
import ai.classifai.util.data.ImageHandler;
import ai.classifai.util.data.StringHandler;
import ai.classifai.util.message.ErrorCodes;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.project.ProjectHandler;
import ai.classifai.util.project.ProjectInfraHandler;
import ai.classifai.util.type.AnnotationHandler;
import ai.classifai.util.type.AnnotationType;
import ai.classifai.util.type.database.H2;
import ai.classifai.util.type.database.RelationalDb;
import ai.classifai.wasabis3.WasabiImageHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Paths;
import java.util.*;

/**
 * General database processing to get high level infos of each created project
 *
 * @author codenamewei
 */
@Slf4j
public class PortfolioVerticle extends AbstractVerticle implements VerticleServiceable
{
    @Setter private static JDBCPool portfolioDbPool;

    public void onMessage(Message<JsonObject> message)
    {
        if (!message.headers().contains(ParamConfig.getActionKeyword()))
        {
            log.error("No action header specified for message with headers {} and body {}",
                    message.headers(), message.body().encodePrettily());

            message.fail(ErrorCodes.NO_ACTION_SPECIFIED.ordinal(), "No keyword " + ParamConfig.getActionKeyword() + " specified");

        }
    }

    public static void createNewProject(@NonNull String projectId)
    {
        ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectId));

        Tuple params = PortfolioVerticle.buildNewProject(loader);

        portfolioDbPool.preparedQuery(PortfolioDbQuery.getCreateNewProject())
                .execute(params)
                .onComplete(fetch -> {

                    if (fetch.succeeded())
                    {
                        String annotation = Objects.requireNonNull(
                                AnnotationHandler.getType(loader.getAnnotationType())).name();
                        log.info("Project " + loader.getProjectName() + " of " + annotation.toLowerCase(Locale.ROOT) + " created");
                    }
                    else
                    {
                        log.debug("Create project failed from database");
                    }
                });
    }

    public static void loadProjectFromImportingConfigFile(@NonNull JsonObject input)
    {
        ProjectLoader loader = PortfolioParser.parseIn(input);

        String newProjName = "";
        while (!ProjectHandler.isProjectNameUnique(loader.getProjectName(), loader.getAnnotationType()))
        {
            newProjName = new NameGenerator().getNewProjectName();
            loader.setProjectName(newProjName);
            loader.setProjectId(UuidGenerator.generateUuid());
        }

        // Only show popup if there is duplicate project name
        if(!newProjName.equals(""))
        {
            String message = "Name Overlapped. Rename as " + newProjName + ".";
            log.info(message);
        }

        ProjectHandler.loadProjectLoader(loader);

        //load project table first
        JsonObject contentJsonObject = input.getJsonObject(ParamConfig.getProjectContentParam());
        ProjectParser.parseIn(loader, contentJsonObject);

        //load portfolio table last
        Tuple params = PortfolioVerticle.buildNewProject(loader);

        portfolioDbPool.preparedQuery(PortfolioDbQuery.getCreateNewProject())
                .execute(params)
                .onComplete(DBUtils.handleEmptyResponse(
                        () -> {
                            ProjectImportSelector.setProjectName(loader.getProjectName());
                            log.info("Import project " + loader.getProjectName() + " success!");
                            },
                        cause -> log.info("Failed to import project " + loader.getProjectName() + " from configuration file")
                        ));

    }

    public static void updateLoaderLabelList(ProjectLoader loader, ProjectVersion project, List<String> newLabelListJson)
    {
        List<String> newLabelList = new ArrayList<>();

        for(String label: newLabelListJson)
        {
            String trimmedLabel = StringHandler.removeEndOfLineChar(label);

            newLabelList.add(trimmedLabel);
        }

        project.setCurrentVersionLabelList(newLabelList);
        loader.setLabelList(newLabelList);
    }

    public static void updateFileSystemUuidList(@NonNull String projectID)
    {
        ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectID));

        List<String> uuidList = loader.getUuidListFromDb();

        ProjectVersion project = loader.getProjectVersion();

        project.setCurrentVersionUuidList(uuidList);

        Tuple updateUuidListBody = Tuple.of(project.getUuidVersionDbFormat(), projectID);

        portfolioDbPool.preparedQuery(PortfolioDbQuery.getUpdateProject())
                .execute(updateUuidListBody)
                .onComplete(reply -> {
                    if (!reply.succeeded())
                    {
                        log.info("Update list of uuids to Portfolio Database failed");
                    }
                });
    }

    public void configProjectLoaderFromDb()
    {
        portfolioDbPool.query(PortfolioDbQuery.getRetrieveAllProjects())
                .execute()
                .onComplete(DBUtils.handleResponse(
                        result -> {
                            if (result.size() == 0) {
                                log.info("No projects founds.");
                            } else {
                                for (Row row : result)
                                {
                                    Version currentVersion = new Version(row.getString(7));

                                    ProjectVersion project = PortfolioParser.loadProjectVersion(row.getString(8));     //project_version

                                    project.setCurrentVersion(currentVersion.getVersionUuid());

                                    Map uuidDict = ActionOps.getKeyWithArray(row.getString(9));
                                    project.setUuidListDict(uuidDict);                                                      //uuid_project_version

                                    Map labelDict = ActionOps.getKeyWithArray(row.getString(10));
                                    project.setLabelListDict(labelDict);                                                    //label_project_version

                                    ProjectLoader loader = ProjectLoader.builder()
                                            .projectId(row.getString(0))                                                   //project_id
                                            .projectName(row.getString(1))                                                 //project_name
                                            .annotationType(row.getInteger(2))                                             //annotation_type
                                            .projectPath(new File(row.getString(3)))                                       //project_path
                                            .projectLoaderStatus(ProjectLoaderStatus.DID_NOT_INITIATED)
                                            .isProjectNew(row.getBoolean(4))                                               //is_new
                                            .isProjectStarred(row.getBoolean(5))                                           //is_starred
                                            .projectInfra(ProjectInfraHandler.getInfra(row.getString(6)))                  //project_infra
                                            .projectVersion(project)                                                            //project_version
                                            .build();

                                    //load each data points
                                    AnnotationVerticle.configProjectLoaderFromDb(loader);
                                    ProjectHandler.loadProjectLoader(loader);
                                }
                            }
                        },
                        cause -> log.info("Retrieving from portfolio database to project loader failed")
                ));
    }

    public void buildProjectFromCLI()
    {
        // To build project from cli
    }

    public void getProjectMetadata(Message<JsonObject> message)
    {
        String projectId = message.body().getString(ParamConfig.getProjectIdParam());

        List<ProjectMetaProperties> result = new ArrayList<>();

        getProjectMetadata(result, projectId);

        JsonObject response = ReplyHandler.getOkReply();
        response.put(ParamConfig.getContent(), result);

        message.replyAndRequest(response);
    }

    public static void getProjectMetadata(@NonNull List<ProjectMetaProperties> result, @NonNull String projectId)
    {
        ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectId));

        Version currentVersion = loader.getProjectVersion().getCurrentVersion();

        File projectPath = loader.getProjectPath();

        if (!projectPath.exists())
        {
            log.info(String.format("Root path of project [%s] is missing! %s does not exist.", loader.getProjectName(), loader.getProjectPath()));
        }

        List<String> existingDataInDir = ImageHandler.getValidImagesFromFolder(projectPath);

        result.add(ProjectMetaProperties.builder()
                .projectName(loader.getProjectName())
                .projectPath(loader.getProjectPath().getAbsolutePath())
                .isNewParam(loader.getIsProjectNew())
                .isStarredParam(loader.getIsProjectStarred())
                .isLoadedParam(loader.getIsLoadedFrontEndToggle())
                .isCloud(loader.isCloud())
                .projectInfraParam(loader.getProjectInfra())
                .createdDateParam(currentVersion.getCreatedDate().toString())
                .lastModifiedDate(currentVersion.getLastModifiedDate().toString())
                .currentVersionParam(currentVersion.getVersionUuid())
                .totalUuidParam(existingDataInDir.size())
                .isRootPathValidParam(projectPath.exists())
                .build()
        );

    }

    public static void updateIsNewParam(@NonNull String projectID)
    {
        Tuple params = Tuple.of(Boolean.FALSE, projectID);

        portfolioDbPool.preparedQuery(PortfolioDbQuery.getUpdateIsNewParam())
                .execute(params)
                .onComplete(DBUtils.handleEmptyResponse(
                        () -> Objects.requireNonNull(ProjectHandler.getProjectLoader(projectID)).setIsProjectNew(Boolean.FALSE),
                        cause -> log.info("Update is_new param for project of projectid: " + projectID + " failed")
                ));
    }

    private static Tuple buildNewProject(@NonNull ProjectLoader loader)
    {
        //version list
        ProjectVersion project = loader.getProjectVersion();

        return Tuple.of(loader.getProjectId(),              //project_id
                loader.getProjectName(),                    //project_name
                loader.getAnnotationType(),                 //annotation_type
                loader.getProjectPath().getAbsolutePath(),  //project_path
                loader.getIsProjectNew(),                   //is_new
                loader.getIsProjectStarred(),               //is_starred
                loader.getProjectInfra().name(),            //project_infra
                project.getCurrentVersion().getDbFormat(),  //current_version
                project.getDbFormat(),                      //version_list
                project.getUuidVersionDbFormat(),           //uuid_version_list
                project.getLabelVersionDbFormat());         //label_version_list

    }

    public static JDBCPool createJDBCPool(Vertx vertx, RelationalDb db)
    {
        return JDBCPool.pool(vertx, new JsonObject()
                .put("url", db.getUrlHeader() + DbConfig.getTableAbsPathDict().get(DbConfig.getPortfolioKey()))
                .put("driver_class", db.getDriver())
                .put("user", db.getUser())
                .put("password", db.getPassword())
                .put("max_pool_size", 30));
    }

    public static ThumbnailProperties queryData(String projectId, String uuid, @NonNull String annotationKey)
    {
        ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectId));
        Annotation annotation = loader.getUuidAnnotationDict().get(uuid);
        DataInfoProperties version = annotation.getAnnotationDict().get(loader.getCurrentVersionUuid());
        Map<String, String> imgData = new HashMap<>();
        String dataPath = "";

        if(loader.isCloud())
        {
            try
            {
                BufferedImage img = WasabiImageHandler.getThumbNail(loader.getWasabiProject(), annotation.getImgPath());

                //not checking orientation for on cloud version
                imgData = ImageHandler.getThumbNail(img);
            }
            catch(Exception e)
            {
                log.debug("Unable to write Buffered Image.");
            }

        }
        else
        {
            dataPath = Paths.get(loader.getProjectPath().getAbsolutePath(), annotation.getImgPath()).toString();

            try
            {
                Mat imageMat  = Imgcodecs.imread(dataPath);

                BufferedImage img = ImageHandler.toBufferedImage(imageMat);

                imgData = ImageHandler.getThumbNail(img);
            }
            catch(Exception e)
            {
                log.debug("Failure in reading image of path " + dataPath, e);
            }
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

        if(annotationKey.equals(ParamConfig.getBoundingBoxParam())) {
            thmbProps.setBoundingBoxParam(new ArrayList<>(version.getAnnotation()));
        } else if(annotationKey.equals(ParamConfig.getSegmentationParam())) {
            thmbProps.setSegmentationParam(new ArrayList<>(version.getAnnotation()));
        }

        return thmbProps;
    }

    public static String getAnnotationKey(ProjectLoader loader) {
        if(loader.getAnnotationType().equals(AnnotationType.BOUNDINGBOX.ordinal())) {
            return ParamConfig.getBoundingBoxParam();
        } else {
            return ParamConfig.getSegmentationParam();
        }
    }

    @Override
    public void stop(Promise<Void> promise)
    {
        portfolioDbPool.close();

        log.info("Portfolio Verticle stopping...");
    }

    //obtain a JDBC pool connection,
    //Performs a SQL query to create the portfolio table unless existed
    @Override
    public void start(Promise<Void> promise)
    {
        H2 h2 = DbConfig.getH2();

        portfolioDbPool = createJDBCPool(vertx, h2);

        portfolioDbPool.getConnection(ar -> {

                if (ar.succeeded()) {
                     portfolioDbPool.query(PortfolioDbQuery.getCreatePortfolioTable())
                            .execute()
                            .onComplete(DBUtils.handleResponse(
                                    result -> {
                                        //the consumer methods registers an event bus destination handler
                                        vertx.eventBus().consumer(PortfolioDbQuery.getQueue(), this::onMessage);

                                        promise.complete();
                                    },
                                    cause -> {
                                        log.error("Portfolio database preparation error", cause);
                                        promise.fail(cause);
                                    }

                            ));
                }
                else
                {
                    log.error("Could not open a portfolio database connection", ar.cause());
                    promise.fail(ar.cause());
                }
        });
    }
}


