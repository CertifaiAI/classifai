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
package ai.classifai.router;

import ai.classifai.action.FileGenerator;
import ai.classifai.database.DbService;
import ai.classifai.router.controller.*;
import ai.classifai.router.controller.annotation.image.BoundingBoxAnnotationController;
import ai.classifai.router.controller.annotation.image.PolygonAnnotationController;
import ai.classifai.router.controller.data.ImageDataController;
import ai.classifai.router.controller.dataversion.ImageDataVersionController;
import ai.classifai.selector.project.LabelFileSelector;
import ai.classifai.selector.project.ProjectFolderSelector;
import ai.classifai.selector.project.ProjectImportSelector;
import ai.classifai.service.*;
import ai.classifai.util.ParamConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * Endpoint routing for different url requests
 *
 * @author codenamewei
 */
@Slf4j
public class EndpointRouter extends AbstractVerticle
{
    //***********Services***************
    private DbService dbService;
    private ProjectService projectService;
    private DataService dataService;
    private ImageService imageService;
    private LabelService labelService;
    private ImageAnnotationService imageAnnotationService;
    private ImageDataVersionService imageDataVersionService;
    private ProjectLoadingService projectLoadingService;

    private ProjectFolderSelector projectFolderSelector;
    private ProjectImportSelector projectImporter;
    private LabelFileSelector labelFileSelector;
    private FileGenerator fileGenerator;

    //***********Controller*************
    private ProjectController projectController;
    private VersionController versionController;
    private ImageDataController imageDataController;
    private ImageDataVersionController imageDataVersionController;
    private BoundingBoxAnnotationController boundingBoxAnnotationController;
    private PolygonAnnotationController polygonAnnotationController;
    private LabelController labelController;
    private PointController pointController;
    private SystemController systemController;


    @Override
    public void stop(Promise<Void> promise)
    {
        log.debug("Endpoint Router Verticle stopping...");

        //add action before stopped if necessary
    }

    private Future<Void> configureServices()
    {
        return vertx.executeBlocking(promise ->
        {
            Thread projectFolder = new Thread(() -> projectFolderSelector = new ProjectFolderSelector());
            projectFolder.start();

            Thread projectImport = new Thread(() -> projectImporter = new ProjectImportSelector());
            projectImport.start();

            Thread labelFileImport = new Thread(() -> labelFileSelector = new LabelFileSelector());
            labelFileImport.start();

            Thread threadZipFileGenerator = new Thread(() -> fileGenerator = new FileGenerator());
            threadZipFileGenerator.start();

            dbService = new DbService(vertx);

            projectService = new ProjectService(vertx);
            dataService = new DataService(vertx);
            imageService = new ImageService(vertx);
            labelService = new LabelService(vertx);
            imageAnnotationService = new ImageAnnotationService(vertx);
            imageService = new ImageService(vertx);
            imageDataVersionService = new ImageDataVersionService(vertx);
            projectLoadingService = new ProjectLoadingService(vertx);

            promise.complete();
        });
    }

    private Future<Void> configureControllers()
    {
        projectController = new ProjectController(vertx, dbService, projectService, dataService, imageService,
                labelService, imageDataVersionService, projectLoadingService);
        imageDataController = new ImageDataController(vertx, dbService, imageService);
        systemController = new SystemController(vertx, projectFolderSelector, projectImporter, labelFileSelector);
        labelController = new LabelController(vertx, labelService);
        imageDataVersionController = new ImageDataVersionController(vertx, imageAnnotationService);

        return Future.succeededFuture();
    }

    private void addNoCacheHeader(RoutingContext ctx)
    {
        ctx.response().headers().add("Cache-Control", "no-cache");
        ctx.next();
    }

    private Future<HttpServer> createHttpServer(Router router)
    {
        return vertx.createHttpServer()
                .requestHandler(router)
                .exceptionHandler(Throwable::printStackTrace)
                .listen(ParamConfig.getHostingPort());
    }

    private void addStaticResourceWithCacheBusting(Router router)
    {
        router.route().handler(this::addNoCacheHeader);
        router.route().handler(StaticHandler.create());
    }

    private void assignAPIToController(Router router)
    {
        final String projectV1Endpoint = "/:annotation_type/projects/:project_name";

        final String projectV2Endpoint = "/v2/:annotation_type/projects/:project_name";

        //*******************************V1 Endpoints*******************************

        router.get("/:annotation_type/projects/meta").handler(projectController::getAllProjectsMeta);

        router.get("/:annotation_type/projects/:project_name/meta").handler(projectController::getProjectMetadata);

        router.get(projectV1Endpoint).handler(projectController::loadProject);

        // FIXME: to be deleted
        router.get("/:annotation_type/projects/:project_name/loadingstatus").handler(projectController::loadProjectStatus);

        router.delete(projectV1Endpoint).handler(projectController::deleteProject);

//        router.get("/:annotation_type/projects/:project_name/uuid/:uuid/thumbnail").handler(imageDataController::getThumbnail);

//        router.get("/:annotation_type/projects/:project_name/uuid/:uuid/imgsrc").handler(imageDataController::getImageSource);


//        router.put("/:annotation_type/projects/:project_name/uuid/:uuid/update").handler(imageDataVersionController::updateData);
//
//        router.put("/:annotation_type/projects/:project_name/newlabels").handler(labelController::updateLabels);

        //*******************************V2 Endpoints*******************************

//        router.put("/v2/newproject").handler(v2::importProject);

//        // FIXME: Endpoint can be improved to "/project/:project_id"
//        router.put(projectV1Endpoint).handler(projectController::closeProjectState);
//
        router.put("/:annotation_type/projects/:project_name/star").handler(projectController::starProject);
//
//        router.put("/v2/:annotation_type/projects/:project_name/reload").handler(projectController::reloadProject);

        // FIXME: to be deleted
        router.get("/v2/:annotation_type/projects/:project_name/reloadstatus").handler(projectController::reloadProjectStatus);
//
//        router.put("/v2/:annotation_type/projects/:project_name/export/:export_type").handler(v2::exportProject);
//
//        router.get("/v2/:annotation_type/projects/importstatus").handler(v2::getImportStatus);
//
//        router.get("/v2/:annotation_type/projects/exportstatus").handler(v2::getExportStatus);

        router.put("/v2/:annotation_type/projects/:project_name/rename/:new_project_name").handler(projectController::renameProject);

        router.put("/v2/labelfiles").handler(systemController::selectLabelFile);

        router.get("/v2/labelfiles").handler(systemController::selectLabelFileStatus);

        router.put("/v2/folders").handler(systemController::selectProjectFolder);

        router.get("/v2/folders").handler(systemController::selectProjectFolderStatus);

        router.put("/v2/projects").handler(projectController::createImageProject);

        // FIXME: to be deleted
        router.get(projectV2Endpoint).handler(projectController::createProjectStatus);

        //*******************************Cloud*******************************

//        router.put("/v2/:annotation_type/wasabi/projects/:project_name").handler(cloud::createWasabiCloudProject);
    }

    private Future<Router> configureRouter()
    {
        Promise<Router> promise = Promise.promise();

        Router router = Router.router(vertx);

        assignAPIToController(router);

        //display for content in webroot
        //uses no-cache header for cache busting, perform revalidation when fetching static assets
        addStaticResourceWithCacheBusting(router);

        promise.complete(router);

        return promise.future();
    }



    @Override
    public void start(Promise<Void> promise)
    {
        configureServices()
                .compose(unused -> configureControllers())
                .compose(unused -> configureRouter())
                .compose(this::createHttpServer)
                .onSuccess(unused -> promise.complete())
                .onFailure(throwable ->
                {
                    log.debug("Failure in creating HTTPServer in ServerVerticle. " + throwable.getMessage());
                    promise.fail(throwable.getMessage());
                });
    }


}
