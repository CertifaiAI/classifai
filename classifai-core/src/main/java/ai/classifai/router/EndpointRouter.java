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

import ai.classifai.action.ProjectExport;
import ai.classifai.database.annotation.AnnotationDB;
import ai.classifai.database.portfolio.PortfolioDB;
import ai.classifai.router.endpoint.*;
import ai.classifai.ui.NativeUI;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.project.ProjectHandler;
import com.zandero.rest.RestRouter;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

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
    ProjectStatisticEndpoint projectStatisticEndpoint;
    AddImageEndpoint addImageEndpoint;

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
        this.operationEndpoint = new OperationEndpoint(portfolioDB, projectHandler);
        this.imageEndpoint = new ImageEndpoint(portfolioDB, projectHandler);
        this.projectEndpoint = new ProjectEndpoint(portfolioDB, annotationDB, ui, projectHandler);
        this.projectMetadataEndpoint = new ProjectMetadataEndpoint(portfolioDB, projectHandler);
        this.dataEndpoint = new DataEndpoint(portfolioDB, projectHandler);
        this.exportProjectEndpoint = new ExportProjectEndpoint(portfolioDB, projectHandler, projectExport);
        this.updateProjectEndpoint = new UpdateProjectEndpoint(portfolioDB, projectHandler);
        this.projectStatisticEndpoint = new ProjectStatisticEndpoint(projectHandler, portfolioDB);
        this.addImageEndpoint = new AddImageEndpoint(projectHandler);
    }

    private void addNoCacheHeader(RoutingContext ctx)
    {
        ctx.response().headers().add("Cache-Control", "no-cache");
        ctx.next();
    }

    private void enableDevelopmentCORS(Router router) {
        Set<String> allowedHeaders = new HashSet<>();
        allowedHeaders.add("Access-Control-Allow-Method");
        allowedHeaders.add("Access-Control-Allow-Origin");
        allowedHeaders.add("Cache-Control");
        allowedHeaders.add("Pragma");
        allowedHeaders.add("Content-Type");
        RestRouter.enableCors(router, "*", false, -1, allowedHeaders);
    }

    @Override
    public void start(Promise<Void> promise)
    {
        configureVersionVertx();

        Router router = RestRouter.register(vertx,
                projectMetadataEndpoint, exportProjectEndpoint, operationEndpoint,
                imageEndpoint, projectEndpoint, dataEndpoint, updateProjectEndpoint, projectStatisticEndpoint,
                addImageEndpoint);

        // Only enable in development
        enableDevelopmentCORS(router);

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
