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
import ai.classifai.database.DbConfig;
import ai.classifai.database.portfolio.PortfolioDB;
import ai.classifai.database.portfolio.PortfolioVerticle;
import ai.classifai.selector.project.LabelFileSelector;
import ai.classifai.selector.project.ProjectFolderSelector;
import ai.classifai.selector.project.ProjectImportSelector;
import ai.classifai.selector.project.VideoFileSelector;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.type.database.H2;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.jdbcclient.JDBCPool;
import lombok.extern.slf4j.Slf4j;

/**
 * Endpoint routing for different url requests
 *
 * @author codenamewei
 */
@Slf4j
public class EndpointRouter extends AbstractVerticle
{
    private ProjectFolderSelector projectFolderSelector;
    private ProjectImportSelector projectImporter;

    private LabelFileSelector labelFileSelector;
    private VideoFileSelector videoFileSelector;
    private FileGenerator fileGenerator;

    V1Endpoint v1 = new V1Endpoint();
    V2Endpoint v2 = new V2Endpoint();

    public EndpointRouter()
    {
        Thread projectFolder = new Thread(() -> projectFolderSelector = new ProjectFolderSelector());
        projectFolder.start();

        Thread projectImport = new Thread(() -> projectImporter = new ProjectImportSelector());
        projectImport.start();

        Thread labelFileImport = new Thread(() -> labelFileSelector = new LabelFileSelector());
        labelFileImport.start();

        Thread videoFileImport = new Thread(() -> videoFileSelector = new VideoFileSelector());
        videoFileImport.start();

        Thread threadZipFileGenerator = new Thread(() -> fileGenerator = new FileGenerator());
        threadZipFileGenerator.start();
    }

    @Override
    public void stop(Promise<Void> promise) {
        log.debug("Endpoint Router Verticle stopping...");

        //add action before stopped if necessary
    }

    private void configureVersionVertx()
    {
        H2 h2 = DbConfig.getH2();
        JDBCPool portFolioPool = PortfolioVerticle.createJDBCPool(vertx, h2);

        v1.setVertx(vertx);
        v1.setPortfolioDB(new PortfolioDB(portFolioPool));

        v2.setVertx(vertx);
        v2.setProjectFolderSelector(projectFolderSelector);
        v2.setProjectImporter(projectImporter);
        v2.setLabelFileSelector(labelFileSelector);
        v2.setVideoFileSelector(videoFileSelector);
        v2.setPortfolioDB(new PortfolioDB(portFolioPool));

        PortfolioVerticle.setFileGenerator(fileGenerator);
    }

    private void addNoCacheHeader(RoutingContext ctx)
    {
        ctx.response().headers().add("Cache-Control", "no-cache");
        ctx.next();
    }

    @Override
    public void start(Promise<Void> promise)
    {
        Router router = Router.router(vertx);

        router.route().handler(io.vertx.ext.web.handler.CorsHandler.create(".*.")
                .allowedMethod(io.vertx.core.http.HttpMethod.GET)
                .allowedMethod(io.vertx.core.http.HttpMethod.POST)
                .allowedMethod(io.vertx.core.http.HttpMethod.OPTIONS)
                .allowedMethod(HttpMethod.DELETE)
                .allowedMethod(io.vertx.core.http.HttpMethod.PUT)
                .allowedHeader("Access-Control-Allow-Method")
                .allowedHeader("Access-Control-Allow-Origin")
                .allowedHeader("Cache-Control")
                .allowedHeader("Pragma")
                .allowedHeader("Content-Type"));

        //display for content in webroot
        //uses no-cache header for cache busting, perform revalidation when fetching static assets
        router.route().handler(this::addNoCacheHeader);
        router.route().handler(StaticHandler.create());

        final String projectV1Endpoint = "/:annotation_type/projects/:project_name";

        final String projectV2Endpoint = "/v2/:annotation_type/projects/:project_name";

        //*******************************V1 Endpoints*******************************

        router.get("/:annotation_type/projects/meta").handler(v1::getAllProjectsMeta);

        router.get("/:annotation_type/projects/:project_name/meta").handler(v1::getProjectMetadata);

        router.get("/:annotation_type/videoprojects/meta").handler(v1::getAllProjectsMeta);

        router.get("/:annotation_type/videoprojects/:project_name/meta").handler(v1::getProjectMetadata);

        router.get(projectV1Endpoint).handler(v1::loadProject);

        router.delete(projectV1Endpoint).handler(v1::deleteProject);

        router.get("/:annotation_type/projects/:project_name/loadingstatus").handler(v1::loadProjectStatus);

        router.get("/:annotation_type/projects/:project_name/uuid/:uuid/thumbnail").handler(v1::getThumbnail);

        router.get("/:annotation_type/projects/:project_name/uuid/:uuid/imgsrc").handler(v1::getImageSource);

        router.put("/:annotation_type/projects/:project_name/uuid/:uuid/update").handler(v1::updateData);

        router.put("/:annotation_type/projects/:project_name/newlabels").handler(v1::updateLabels);

        //*******************************V2 Endpoints*******************************

        router.put(projectV1Endpoint).handler(v2::closeProjectState);

        router.put("/:annotation_type/projects/:project_name/star").handler(v2::starProject);

        router.put("/v2/:annotation_type/projects/:project_name/reload").handler(v2::reloadProject);

        router.get("/v2/:annotation_type/projects/:project_name/reloadstatus").handler(v2::reloadProjectStatus);

        router.put("/v2/:annotation_type/projects/:project_name/export/:export_type").handler(v2::exportProject);

        router.get("/v2/:annotation_type/projects/importstatus").handler(v2::getImportStatus);

        router.get("/v2/:annotation_type/projects/exportstatus").handler(v2::getExportStatus);

        router.put("/v2/:annotation_type/projects/:project_name/rename/:new_project_name").handler(v2::renameProject);

        router.put("/v2/:annotation_type/projects/:project_name/imgsrc/rename").handler(v2::renameData);

        router.put("/v2/labelfiles").handler(v2::selectLabelFile);

        router.get("/v2/labelfiles").handler(v2::selectLabelFileStatus);

        router.put("/v2/folders").handler(v2::selectProjectFolder);

        router.get("/v2/folders").handler(v2::selectProjectFolderStatus);

        router.put("/v2/projects").handler(v2::createProject);

        router.get(projectV2Endpoint).handler(v2::createProjectStatus);

        router.delete("/v2/:annotation_type/projects/:project_name/uuids").handler(v2::deleteProjectData);

        router.put("/v2/close").handler(v2::closeClassifai);

        router.put("/v2/videofiles").handler(v2::selectVideoFile);

        router.get("/v2/videofiles").handler(v2::selectVideoFileStatus);

        router.post("/v2/:annotation_type/projects/:project_name/extract").handler(v2::initiateVideoFramesExtraction);

        router.get("/v2/:annotation_type/projects/:project_name/extractstatus").handler(v2::videoFramesExtractionStatus);

        router.put("/v2/videoprojects").handler(v2::createProject);

        //*******************************Cloud*******************************

        vertx.createHttpServer()
                .requestHandler(router)
                .exceptionHandler(Throwable::printStackTrace)
                .listen(ParamConfig.getHostingPort(), r -> {

                    if (r.succeeded())
                    {
                        configureVersionVertx();
                        promise.complete();
                    }
                    else {
                        log.debug("Failure in creating HTTPServer in ServerVerticle. " + r.cause().getMessage());
                        promise.fail(r.cause());
                    }
                });
    }
}
