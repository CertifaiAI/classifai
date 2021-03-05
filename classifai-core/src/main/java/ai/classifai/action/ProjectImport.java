/*
 * Copyright (c) 2021 CertifAI Sdn. Bhd.
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
package ai.classifai.action;

import ai.classifai.database.portfolio.PortfolioVerticle;
import io.vertx.core.json.JsonObject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileReader;

/**
 * Import of project from configuration file
 *
 * @author codenamewei
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProjectImport
{
    public static void importProjectFile(@NonNull File jsonFile)
    {
        try
        {
            String jsonStr = IOUtils.toString(new FileReader(jsonFile));

            JsonObject inputJsonObject = new JsonObject(jsonStr);

            PortfolioVerticle.loadProjectFromImportingConfigFile(inputJsonObject);

        }
        catch(Exception e)
        {
            log.info("Error in importing project. ", e);
        }
    }
}