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

import ai.classifai.config.CLIArgument;
import ai.classifai.database.DatabaseMigration;
import ai.classifai.database.config.H2DatabaseConfig;
import ai.classifai.database.config.HsqlDatabaseConfig;
import ai.classifai.util.ParamConfig;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/***
 * Main program to start Classifai
 *
 * @author codenamewei
 */
@Slf4j
public class ClassifaiApp
{
    public static void main(String[] args) throws Exception
    {
        if( !H2DatabaseConfig.isDatabaseExist() && HsqlDatabaseConfig.isDatabaseExist()){
            log.info("Database migration required. Executing database migration. ");
            if (! DatabaseMigration.migrate()){
                log.error("Database migration failed. Old data will not be migrated. You can choose to move on with empty database, or close Classifai now to prevent data lost.");
            }
            else{
                log.info("Database migration is successful!");
            }
        }

        CLIArgument argumentSelector = new CLIArgument(args);

        if(!argumentSelector.isDbSetup())
        {
            log.info("Classifai failed to configure. Abort.");
            return;
        }

        VertxOptions vertxOptions = new VertxOptions();

        vertxOptions.setMaxEventLoopExecuteTimeUnit(TimeUnit.SECONDS);
        vertxOptions.setMaxEventLoopExecuteTime(15); //for bulk images upload

        DeploymentOptions opt = new DeploymentOptions();
        opt.setWorker(true);

        opt.setConfig(new JsonObject().put("http.port", ParamConfig.getHostingPort()));

        Vertx vertx = Vertx.vertx(vertxOptions);
        vertx.deployVerticle(ai.classifai.MainVerticle.class.getName(), opt);

    }
}