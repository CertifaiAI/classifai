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

import ai.classifai.selector.annotation.ToolFileSelector;
import ai.classifai.selector.annotation.ToolFolderSelector;
import ai.classifai.selector.project.ProjectFolderSelector;
import ai.classifai.selector.project.ProjectImportSelector;
import ai.classifai.util.ParamConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
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
    private ToolFileSelector fileSelector;
    private ToolFolderSelector folderSelector;
    private ProjectFolderSelector projectFolderSelector;
    private ProjectImportSelector projectImporter;

    V1Endpoint v1 = new V1Endpoint();
    V2Endpoint v2 = new V2Endpoint();

    CloudEndpoint cloud = new CloudEndpoint();

    public EndpointRouter()
    {
        Thread threadfile = new Thread(() -> fileSelector = new ToolFileSelector());
        threadfile.start();

        Thread threadfolder = new Thread(() -> folderSelector = new ToolFolderSelector());
        threadfolder.start();

        Thread projectFolder = new Thread(() -> projectFolderSelector = new ProjectFolderSelector());
        projectFolder.start();

        Thread projectImport = new Thread(() -> projectImporter = new ProjectImportSelector());
        projectImport.start();
    }

    @Override
    public void stop(Promise<Void> promise) {
        log.debug("Endpoint Router Verticle stopping...");

        //add action before stopped if necessary
    }

    private void configureVersionVertx()
    {
        v1.setVertx(vertx);
        v1.setFolderSelector(folderSelector);
        v1.setFileSelector(fileSelector);

        v2.setVertx(vertx);
        v2.setProjectFolderSelector(projectFolderSelector);
        v2.setProjectImporter(projectImporter);

        cloud.setVertx(vertx);
    }

    @Override
    public void start(Promise<Void> promise)
    {
        Router router = Router.router(vertx);

        //display for content in webroot
        router.route().handler(StaticHandler.create());

        final String projectEndpoint = "/:annotation_type/projects/:project_name";

        //*******************************V1 Endpoints*******************************

        router.get("/:annotation_type/projects").handler(v1::getAllProjects);

        router.get("/:annotation_type/projects/meta").handler(v1::getAllProjectsMeta);

        router.get("/:annotation_type/projects/:project_name/meta").handler(v1::getProjectMetadata);

        router.put("/:annotation_type/newproject/:project_name").handler(v1::createV1NewProject);

        router.get(projectEndpoint).handler(v1::loadProject);

        router.delete(projectEndpoint).handler(v1::deleteProject);

        router.delete("/:annotation_type/projects/:project_name/uuids").handler(v1::deleteProjectUUID);

        router.get("/:annotation_type/projects/:project_name/loadingstatus").handler(v1::loadProjectStatus);

        router.get("/:annotation_type/projects/:project_name/filesys/:file_sys").handler(v1::selectFileSystemType);

        router.get("/:annotation_type/projects/:project_name/filesysstatus").handler(v1::getFileSystemStatus);

        router.get("/:annotation_type/projects/:project_name/uuid/:uuid/thumbnail").handler(v1::getThumbnail);

        router.get("/:annotation_type/projects/:project_name/uuid/:uuid/imgsrc").handler(v1::getImageSource);

        router.put("/:annotation_type/projects/:project_name/uuid/:uuid/update").handler(v1::updateData);

        router.put("/:annotation_type/projects/:project_name/newlabels").handler(v1::updateLabels);

        //*******************************V2 Endpoints*******************************

        router.put("/v2/newproject").handler(v2::importProject);

        router.put(projectEndpoint).handler(v2::closeProjectState);

        router.put("/:annotation_type/projects/:project_name/star").handler(v2::starProject);

        router.put("/v2/:annotation_type/newproject/:project_name").handler(v2::createProject);

        router.put("/v2/:annotation_type/projects/:project_name/reload").handler(v2::reloadProject);

        router.get("/v2/:annotation_type/projects/:project_name/reloadstatus").handler(v2::reloadProjectStatus);

        router.put("/v2/:annotation_type/projects/:project_name/export").handler(v2::exportProject);

        //*******************************Cloud*******************************

        router.put("/v2/:annotation_type/s3/newproject/:project_name").handler(cloud::createS3CloudProject);



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
