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

package ai.certifai;

import ai.certifai.config.PortSelector;
import ai.certifai.database.DatabaseConfig;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.io.File;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ClassifaiApp
{
    public static void main(String[] args)
    {
        if((new File(DatabaseConfig.PORTFOLIO_LCKFILE).exists()) || new File(DatabaseConfig.PROJECT_DB).exists())
        {
            log.info("Database is being used. Probably another application is running. Abort.");
            return;
        }

        configure(args);

        VertxOptions vertxOptions = new VertxOptions();

        vertxOptions.setMaxEventLoopExecuteTimeUnit(TimeUnit.SECONDS);
        vertxOptions.setMaxEventLoopExecuteTime(15); //for bulk images upload

        DeploymentOptions opt = new DeploymentOptions();
        opt.setWorker(true);
        opt.setConfig(new JsonObject().put("http.port", PortSelector.getHostingPort()));

        Vertx vertx = Vertx.vertx(vertxOptions);
        vertx.deployVerticle(ai.certifai.MainVerticle.class.getName(), opt);
    }

    static void configure(String[] args)
    {
        for(int i = 0; i < args.length; ++i)
        {
            if(args[i].contains("--port="))
            {
                String[] buffer = args[i].split("=");
                PortSelector.configurePort(buffer[1]);
            }
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e)
        {
            log.error("Error in setting UIManager: ", e);
        }

    }
}