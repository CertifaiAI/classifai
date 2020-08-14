/*
 * Copyright (c) 2020 CertifAI
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
import ai.classifai.database.portfolio.PortfolioVerticle;
import ai.classifai.database.project.ProjectVerticle;
import ai.classifai.server.ServerVerticle;
import ai.classifai.ui.WelcomeConsole;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class MainVerticle extends AbstractVerticle
{
    public void configureDatabase()
    {
        File dataRootPath = new File(DatabaseConfig.DB_ROOT_PATH);

        if(!dataRootPath.exists())
        {
            boolean databaseIsBuild = dataRootPath.mkdir();

            if(!databaseIsBuild) log.error("Database could not created");
        }
    }

    @Override
    public void start(Promise<Void> promise) {

        configureDatabase();

        Promise<String> portfolioDeployment = Promise.promise();
        vertx.deployVerticle(new PortfolioVerticle(), portfolioDeployment);

        portfolioDeployment.future().compose(id_ -> {

            Promise<String> profileDeployment = Promise.promise();
            vertx.deployVerticle(new ProjectVerticle(), profileDeployment);
            return profileDeployment.future();

        }).compose(id_ -> {

            Promise<String> serverDeployment = Promise.promise();
            vertx.deployVerticle(new ServerVerticle(), serverDeployment);
            return serverDeployment.future();

        }).onComplete(ar ->
        {
            if (ar.succeeded()) {

                WelcomeConsole.start();

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
