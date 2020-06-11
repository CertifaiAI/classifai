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

import ai.certifai.server.ServerConfig;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class IntellibelApp {
    public static void main(String[] args)
    {
        ServerConfig.dynamicPort = ServerConfig.SERVER_PORT;

        for(int i=0; i < args.length; ++i)
        {
            if(args[i].contains("--port="))
            {
                String[] buffer = args[i].split("=");
                if((buffer[1].length() > 0) && (buffer[1].matches("[0-9]+")))
                {
                    ServerConfig.dynamicPort = Integer.parseInt(buffer[1]);
                }
            }
        }

        VertxOptions vertxOptions = new VertxOptions();

        vertxOptions.setMaxEventLoopExecuteTimeUnit(TimeUnit.SECONDS);
        vertxOptions.setMaxEventLoopExecuteTime(15);

        //vertxOptions.setMaxWorkerExecuteTimeUnit(TimeUnit.SECONDS);
        //vertxOptions.setMaxWorkerExecuteTime(15);

        System.out.println("getMaxEventLoopExecuteTimeUnit: " + vertxOptions.getMaxEventLoopExecuteTimeUnit());
        System.out.println("getMaxEventLoopExecuteTime: " + vertxOptions.getMaxEventLoopExecuteTime());
        System.out.println("getMaxWorkerExecuteTime: " + vertxOptions.getMaxWorkerExecuteTime());

        //setWarningExceptionTimeUnit
        //setMaxEventLoopExecuteTimeUnit

        DeploymentOptions opt = new DeploymentOptions();
        opt.setWorker(true);
        opt.setConfig(new JsonObject().put("http.port", ServerConfig.dynamicPort));


        Vertx vertx = Vertx.vertx(vertxOptions);
        vertx.deployVerticle(ai.certifai.MainVerticle.class.getName(), opt);
    }
}