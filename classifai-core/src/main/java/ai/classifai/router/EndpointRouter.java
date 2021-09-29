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
import ai.classifai.util.ParamConfig;
import ai.classifai.util.type.database.H2;
import com.zandero.rest.RestRouter;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
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
    private FileGenerator fileGenerator;

    V1Endpoint v1 = new V1Endpoint();
    V2Endpoint v2 = new V2Endpoint();

    CloudEndpoint cloud = new CloudEndpoint();

    public EndpointRouter()
    {
        Thread projectFolder = new Thread(() -> projectFolderSelector = new ProjectFolderSelector());
        projectFolder.start();

        Thread projectImport = new Thread(() -> projectImporter = new ProjectImportSelector());
        projectImport.start();

        Thread labelFileImport = new Thread(() -> labelFileSelector = new LabelFileSelector());
        labelFileImport.start();

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
        v2.setPortfolioDB(new PortfolioDB(portFolioPool));

        cloud.setVertx(vertx);

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
        Router router = RestRouter.register(vertx, v1, v2);

        router.route().handler(this::addNoCacheHeader);
        router.route().handler(StaticHandler.create());

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
