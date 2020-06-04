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
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

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
        DeploymentOptions opt = new DeploymentOptions();
        opt.setConfig(new JsonObject().put("http.port", ServerConfig.dynamicPort));
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(ai.certifai.MainVerticle.class.getName(), opt);
    }
}