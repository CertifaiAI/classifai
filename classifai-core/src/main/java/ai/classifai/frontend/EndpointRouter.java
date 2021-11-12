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
package ai.classifai.frontend;

import ai.classifai.backend.action.ProjectExport;
import ai.classifai.backend.database.annotation.AnnotationDB;
import ai.classifai.backend.database.portfolio.PortfolioDB;
import ai.classifai.core.ExportProjectService;
import ai.classifai.core.ImageDataService;
import ai.classifai.core.ProjectOperationService;
import ai.classifai.core.util.ParamConfig;
import ai.classifai.core.util.project.ProjectHandler;
import ai.classifai.frontend.endpoint.*;
import ai.classifai.frontend.ui.NativeUI;
import com.zandero.rest.RestRouter;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
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
    private final NativeUI ui;
    private final ProjectHandler projectHandler;
    private final ProjectExport projectExport;
    private final PortfolioDB portfolioDB;
    private final AnnotationDB annotationDB;

    OperationEndpoint operationEndpoint;
    ImageEndpoint imageEndpoint;
    ProjectEndpoint projectEndpoint;
    ProjectMetadataEndpoint projectMetadataEndpoint;
    DataEndpoint dataEndpoint;
    ExportProjectEndpoint exportProjectEndpoint;
    UpdateProjectEndpoint updateProjectEndpoint;

    public EndpointRouter(NativeUI ui, PortfolioDB portfolioDB, AnnotationDB annotationDB, ProjectHandler projectHandler, ProjectExport projectExport)
    {
        this.ui = ui;
        this.projectHandler = projectHandler;
        this.portfolioDB = portfolioDB;
        this.annotationDB = annotationDB;
        this.projectExport = projectExport;
    }

    @Override
    public void stop(Promise<Void> promise) {
        log.debug("Endpoint Router Verticle stopping...");

        //add action before stopped if necessary
    }

    private void configureVersionVertx()
    {
        ProjectOperationService projectOperationService = new ProjectOperationService(portfolioDB, projectHandler, annotationDB);

        this.operationEndpoint = new OperationEndpoint(projectOperationService);
        this.imageEndpoint = new ImageEndpoint(new ImageDataService(portfolioDB, projectHandler));
        this.projectEndpoint = new ProjectEndpoint(ui, projectOperationService);
        this.projectMetadataEndpoint = new ProjectMetadataEndpoint(projectOperationService);
        this.dataEndpoint = new DataEndpoint(projectOperationService);
        this.exportProjectEndpoint = new ExportProjectEndpoint(new ExportProjectService(portfolioDB, projectHandler, projectExport));
        this.updateProjectEndpoint = new UpdateProjectEndpoint(projectOperationService);
    }

    private void addNoCacheHeader(RoutingContext ctx)
    {
        ctx.response().headers().add("Cache-Control", "no-cache");
        ctx.next();
    }

    @Override
    public void start(Promise<Void> promise)
    {
        configureVersionVertx();
        Router router = RestRouter.register(vertx,
                projectMetadataEndpoint, exportProjectEndpoint, operationEndpoint,
                imageEndpoint, projectEndpoint, dataEndpoint, updateProjectEndpoint);

        router.route().handler(this::addNoCacheHeader);
        router.route().handler(StaticHandler.create());

        vertx.createHttpServer()
                .requestHandler(router)
                .exceptionHandler(Throwable::printStackTrace)
                .listen(ParamConfig.getHostingPort(), r -> {

                    if (r.succeeded())
                    {
                        promise.complete();
                    }
                    else {
                        log.debug("Failure in creating HTTPServer in ServerVerticle. " + r.cause().getMessage());
                        promise.fail(r.cause());
                    }
                });
    }
}
