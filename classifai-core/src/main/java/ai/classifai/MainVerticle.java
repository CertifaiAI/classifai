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

import ai.classifai.database.DbOps;
import ai.classifai.database.annotation.bndbox.BoundingBoxVerticle;
import ai.classifai.database.annotation.seg.SegVerticle;
import ai.classifai.database.portfolio.PortfolioVerticle;
import ai.classifai.router.EndpointRouter;
import ai.classifai.ui.launcher.LogoLauncher;
import ai.classifai.ui.launcher.RunningStatus;
import ai.classifai.ui.launcher.WelcomeLauncher;
import ai.classifai.util.ParamConfig;
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
    private static PortfolioVerticle portfolioVerticle;
    private static BoundingBoxVerticle boundingBoxVerticle;
    private static SegVerticle segVerticle;
    private static EndpointRouter serverVerticle;

    static
    {
        portfolioVerticle = new PortfolioVerticle();
        boundingBoxVerticle = new BoundingBoxVerticle();
        segVerticle = new SegVerticle();
        serverVerticle = new EndpointRouter();
    }

    @Override
    public void start(Promise<Void> promise)
    {
        if (!ParamConfig.isDockerEnv()) WelcomeLauncher.start();

        DbOps.configureDatabase();

        Promise<String> portfolioDeployment = Promise.promise();
        vertx.deployVerticle(portfolioVerticle, portfolioDeployment);

        portfolioDeployment.future().compose(id_ -> {

            Promise<String> bndBoxDeployment = Promise.promise();
            vertx.deployVerticle(boundingBoxVerticle, bndBoxDeployment);
            return bndBoxDeployment.future();

        }).compose(id_ -> {

            Promise<String> segDeployment = Promise.promise();
            vertx.deployVerticle(segVerticle, segDeployment);

            return segDeployment.future();

        }).compose(id_ -> {

            Promise<String> serverDeployment = Promise.promise();
            vertx.deployVerticle(serverVerticle, serverDeployment);
            return serverDeployment.future();

        }).onComplete(ar -> {

            if (ar.succeeded())
            {
                portfolioVerticle.buildProjectFromCLI();

                LogoLauncher.print();

                log.info("Classifai started successfully");
                log.info("Go on and open http://localhost:" + config().getInteger("http.port"));

                //docker environment not enabling welcome launcher
                if (!ParamConfig.isDockerEnv())
                {
                    try
                    {
                        WelcomeLauncher.setRunningStatus(RunningStatus.RUNNING);
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

    public static void closeVerticles()
    {
        try
        {
            boundingBoxVerticle.stop(Promise.promise());
            segVerticle.stop(Promise.promise());
            serverVerticle.stop(Promise.promise());
        }
        catch (Exception e)
        {
            log.info("Error when stopping verticles: ", e);
        }
    }

    @Override
    public void stop(Promise<Void> promise) throws Exception
    {
        vertx.close( r -> {if(r.succeeded()){
            log.info("Classifai close successfully");
        }});
    }
}
