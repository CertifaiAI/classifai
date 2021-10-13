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
package ai.classifai;

import ai.classifai.action.ProjectExport;
import ai.classifai.action.ProjectImport;
import ai.classifai.database.DbConfig;
import ai.classifai.database.DbOps;
import ai.classifai.database.JDBCPoolHolder;
import ai.classifai.database.annotation.AnnotationDB;
import ai.classifai.database.portfolio.PortfolioDB;
import ai.classifai.database.wasabis3.WasabiVerticle;
import ai.classifai.loader.CLIProjectInitiator;
import ai.classifai.router.EndpointRouter;
import ai.classifai.ui.ContainerUI;
import ai.classifai.ui.DesktopUI;
import ai.classifai.ui.NativeUI;
import ai.classifai.ui.enums.RunningStatus;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.project.ProjectHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

/**
 * Main verticle to create multiple verticles
 *
 * @author codenamewei
 */
@Slf4j
public class MainVerticle extends AbstractVerticle
{
    private final WasabiVerticle wasabiVerticle;
    private final EndpointRouter serverVerticle;
    private final NativeUI ui;
    private final ProjectHandler projectHandler;
    private final JDBCPoolHolder jdbcPoolHolder;
    private final PortfolioDB portfolioDB;

    public MainVerticle(CLIProjectInitiator initiator){
        jdbcPoolHolder = new JDBCPoolHolder();

        /* TODO: fix circular dependency */
        final ProjectImport projectImport = new ProjectImport(null, null, null);
        if(ParamConfig.isDockerEnv()) {
            ui = new ContainerUI();
        } else {
            ui = new DesktopUI(this::closeVerticles, projectImport);
        }
        this.projectHandler = new ProjectHandler(ui, initiator);
        final ProjectExport projectExport = new ProjectExport(projectHandler);

        final AnnotationDB annotationDB = new AnnotationDB(jdbcPoolHolder, projectHandler, null/* TODO: fix circular dependency */);
        this.portfolioDB = new PortfolioDB(jdbcPoolHolder, projectHandler, projectExport, annotationDB);
        annotationDB.setPortfolioDB(portfolioDB);

        //TODO: Fix circular dependency
        projectImport.setProjectHandler(projectHandler);
        projectImport.setAnnotationDB(annotationDB);
        projectImport.setPortfolioDB(portfolioDB);


        wasabiVerticle = new WasabiVerticle(projectHandler, portfolioDB, annotationDB);
        serverVerticle = new EndpointRouter(ui, portfolioDB, annotationDB, projectHandler, projectExport);
    }

    @Override
    public void start(Promise<Void> promise)
    {
        ui.start();
        new DbOps(ui).configureDatabase();

        Promise<String> serverDeployment = Promise.promise();
        vertx.deployVerticle(serverVerticle, serverDeployment);

        serverDeployment.future().compose(id_ -> {
            Promise<String> wasabiDeployment = Promise.promise();
            vertx.deployVerticle(wasabiVerticle, wasabiDeployment);
            return wasabiDeployment.future();

        }).onComplete(ar -> {
            jdbcPoolHolder.init(vertx, DbConfig.getH2());

            if (ar.succeeded())
            {
                portfolioDB.configProjectLoaderFromDb();
                portfolioDB.buildProjectFromCLI();

                printLogo();

                log.info("Classifai started successfully");
                log.info("Go on and open http://localhost:" + ParamConfig.getHostingPort());

                //docker environment not enabling welcome launcher
                if (!ParamConfig.isDockerEnv())
                {
                    try
                    {
                        ui.setRunningStatus(RunningStatus.RUNNING);
                    }
                    catch (Exception e)
                    {
                        log.info("Welcome Launcher failed to launch: ", e);
                    }
                }

                promise.complete();

            }
            else
            {
                promise.fail(ar.cause());
            }
        });

    }

    public void closeVerticles()
    {
        try
        {
            serverVerticle.stop(Promise.promise());
            wasabiVerticle.stop(Promise.promise());
        }
        catch (Exception e)
        {
            log.info("Error when stopping verticles: ", e);
        }
    }

    @Override
    public void stop(Promise<Void> promise) throws Exception
    {
        jdbcPoolHolder.stop();
        vertx.close( r -> {if(r.succeeded()){
            log.info("Classifai close successfully");
        }});
    }

    public static void printLogo()
    {
        log.info("\n");
        log.info("   *********  ***          *****     *********  *********  *********  *********    *****    *********  ");
        log.info("   *********  ***        *********   ***        ***        *********  *********  *********  *********  ");
        log.info("   ***        ***        ***    ***  ***        ***           ***     ***        ***   ***     ***     ");
        log.info("   ***        ***        ***    ***  *********  *********     ***     *********  ***   ***     ***     ");
        log.info("   ***        ***        **********        ***        ***     ***     *********  *********     ***     ");
        log.info("   *********  *********  ***    ***        ***        ***  *********  ***        ***   ***  *********  ");
        log.info("   *********  *********  ***    ***  *********  *********  *********  ***        ***   ***  *********  ");
        log.info("\n");
    }
}
