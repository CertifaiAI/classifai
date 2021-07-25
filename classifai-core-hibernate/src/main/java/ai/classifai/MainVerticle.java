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

import ai.classifai.router.EndpointRouter;
import ai.classifai.ui.component.LookFeelSetter;
import ai.classifai.ui.launcher.LogoLauncher;
import ai.classifai.ui.launcher.RunningStatus;
import ai.classifai.ui.launcher.WelcomeLauncher;
import ai.classifai.util.ParamConfig;
import io.vertx.core.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Main verticle to create multiple verticles
 *
 * @author codenamewei
 */
@Slf4j
public class MainVerticle extends AbstractVerticle
{
    private static EndpointRouter serverVerticle;

    static
    {
        serverVerticle = new EndpointRouter();
    }


    private Future<Void> deployOtherVerticles()
    {
        Future<String> deployServerVerticle = Future.future(promise -> vertx.deployVerticle(serverVerticle, promise));
        // TODO: deploy other verticles here

        return deployServerVerticle.mapEmpty();
    }

    private void logAppStart()
    {
        LogoLauncher.print();

        log.info("Classifai started successfully");
        log.info("Go on and open http://localhost:" + ParamConfig.getHostingPort());

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
    }

    @Override
    public void start(Promise<Void> promise)
    {
        if(!ParamConfig.isDockerEnv()) LookFeelSetter.setDarkMode(); //to align dark mode for windows

        if (!ParamConfig.isDockerEnv()) WelcomeLauncher.start();

        Handler<Void> successHandler = unused ->
        {
            logAppStart();
            promise.complete();
        };

        deployOtherVerticles()
                .onSuccess(successHandler)
                .onFailure(result -> promise.fail(result.getCause()));
    }

    public static void closeVerticles()
    {
        try
        {
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
