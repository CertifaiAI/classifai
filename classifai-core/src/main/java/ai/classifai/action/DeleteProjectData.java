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
import ai.classifai.loader.ProjectLoader;
import ai.classifai.util.collection.ConversionHandler;
import io.vertx.core.json.JsonArray;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

/**
 * Utility class for delete data
 *
 * @author devenyantis
 */
@Slf4j
public class DeleteProjectData {

    private DeleteProjectData() {
        throw new IllegalStateException("Utility class");
    }

    public static List<String> deleteProjectDataOnComplete(ProjectLoader loader, List<String> deleteUUIDList, JsonArray deletedDataPath) throws IOException {
        List<String> dbUUIDList = loader.getUuidListFromDb();
        List<String> deletedDataPathList = ConversionHandler.jsonArray2StringList(deletedDataPath);
        if (dbUUIDList.removeAll(deleteUUIDList))
        {
            loader.setUuidListFromDb(dbUUIDList);

            List<String> sanityUUIDList = loader.getSanityUuidList();

            if (sanityUUIDList.removeAll(deleteUUIDList))
            {
                loader.setSanityUuidList(sanityUUIDList);
                FileMover.moveFileToDirectory(loader.getProjectPath().toString(), deletedDataPathList);
            }
            else
            {
                log.info("Error in removing uuid list");
            }

            //update Portfolio Verticle
            PortfolioVerticle.updateFileSystemUuidList(loader.getProjectId());

            return loader.getSanityUuidList();
        }

        return null;
    }

}
