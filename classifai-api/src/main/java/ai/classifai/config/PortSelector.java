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
package ai.classifai.config;

import ai.classifai.server.ParamConfig;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Random;

/**
 * Port Configuration for hosting classifai
 *
 * @author Chiawei Lim
 */
@Slf4j
public class PortSelector {

    @Getter private static Integer hostingPort;
    private final static Integer DEFAULT_PORT = 9999;
    private final static Integer MIN = 9000;
    private final static Integer MAX = DEFAULT_PORT;

    static
    {
        setHostingPort(DEFAULT_PORT);

        //checkPort();
    }

    public static void configurePort(@NonNull String inputArg)
    {
        if((inputArg != null) && (inputArg.length() > 0) && (inputArg.matches("[0-9]+")))
        {
            setHostingPort(Integer.parseInt(inputArg));
        }

        //checkPort();
    }

    private static ServerSocket checkPortSanity(Integer port)
    {
        try {
            return new ServerSocket(port);
        }
        catch (IOException e) {
            // if the program gets here, no port in the range was found
            log.info("IOException error during configure port " + port, e);
            return null;
        }
    }

    private static void checkPort()
    {
        if(checkPortSanity(hostingPort) == null)
        {
            for(int i = 0 ; i < 50; ++i) // try 50 times to get a port to host
            {
                ServerSocket socket = checkPortSanity(getRandomNumberInRange(MIN, MAX));

                if(socket != null)
                {
                    setHostingPort(socket.getLocalPort());
                    break;
                }
            }

            log.info("Error: Port could not be correctly configured. Program expected to not work fine.");
        }
    }

    private static void setHostingPort(Integer port) {
        hostingPort = port;
        ParamConfig.setHostingPort(port);
    }

    private static Integer getRandomNumberInRange(Integer min, Integer max) {

        if (min >= max) {
            throw new IllegalArgumentException("Max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

}
