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

import ai.classifai.config.DbConfig;
import ai.classifai.config.PortSelector;
import ai.classifai.util.ParamConfig;
import com.formdev.flatlaf.FlatLightLaf;
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
        boolean isConfigured = configure(args);

        if (isConfigured == false)
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

    static boolean configure(String[] args)
    {
        boolean removeDbLock = false;
        boolean isDockerEnv = false;

        for(int i = 0; i < args.length; ++i)
        {
            String arg = args[i];
            if(arg.contains("--port="))
            {
                String[] buffer = args[i].split("=");
                PortSelector.configurePort(buffer[1]);
            }
            else if(arg.contains("--unlockdb"))
            {
                removeDbLock = true;
            }
            else if(arg.contains("--docker"))
            {
                isDockerEnv = true;
                ParamConfig.setIsDockerEnv(true);
            }
        }
      
        if(!isDockerEnv) FlatLightLaf.install();

        return DbConfig.isDatabaseSetup(removeDbLock);
    }
}
