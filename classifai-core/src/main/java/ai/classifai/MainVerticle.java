/*
 * Copyright (c) 2020 CertifAI Sdn. Bhd.
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

import ai.classifai.database.DatabaseConfig;
import ai.classifai.database.annotation.bndbox.BoundingBoxVerticle;
import ai.classifai.database.annotation.seg.SegVerticle;
import ai.classifai.database.portfoliodb.PortfolioVerticle;
import ai.classifai.router.EndpointRouter;
import ai.classifai.ui.launcher.LogoLauncher;
import ai.classifai.ui.launcher.WelcomeLauncher;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * Main verticle to create multiple verticles
 *
 * @author Chiawei Lim
 */
@Slf4j
public class MainVerticle extends AbstractVerticle
{
    public void configureDatabase()
    {
        File dataRootPath = new File(DatabaseConfig.getDbRootPath());

        if(dataRootPath.exists())
        {
            log.info("Existing database of classifai on " + dataRootPath);
        }
        else
        {
            log.info("Database of classifai created on " + dataRootPath);

            boolean databaseIsBuild = dataRootPath.mkdir();

            if(!databaseIsBuild)
            {
                log.debug("Root database could not created: ", dataRootPath);
            }
        }
    }

    @Override
    public void start(Promise<Void> promise) {

        configureDatabase();

        Promise<String> portfolioDeployment = Promise.promise();
        vertx.deployVerticle(new PortfolioVerticle(), portfolioDeployment);

        portfolioDeployment.future().compose(id_ -> {

            Promise<String> bndBoxDeployment = Promise.promise();
            vertx.deployVerticle(new BoundingBoxVerticle(), bndBoxDeployment);
            return bndBoxDeployment.future();

        }).compose(id_ -> {

            Promise<String> segDeployment = Promise.promise();
            vertx.deployVerticle(new SegVerticle(), segDeployment);

            return segDeployment.future();

        }).compose(id_ -> {

            Promise<String> serverDeployment = Promise.promise();
            vertx.deployVerticle(new EndpointRouter(), serverDeployment);
            return serverDeployment.future();

        }).onComplete(ar ->
        {
            if (ar.succeeded()) {

                LogoLauncher.print();
                
                WelcomeLauncher.start();

                log.info("Classifai started successfully");
                log.info("Go on and open http://localhost:" + config().getInteger("http.port"));

                promise.complete();

            } else {
                promise.fail(ar.cause());
            }
        });
    }

    @Override
    public void stop(Promise<Void> promise) throws Exception
    {
        vertx.close( r -> {if(r.succeeded()){
            log.info("Classifai close successfully");
        }});
    }
}
