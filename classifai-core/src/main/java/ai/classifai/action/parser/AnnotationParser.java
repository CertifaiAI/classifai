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
package ai.classifai.action.parser;

import ai.classifai.database.annotation.AnnotationVerticle;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.util.ParamConfig;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.Tuple;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/***
 * Parsing Project Table in and out classifai with configuration file
 *
 * @author codenamewei
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AnnotationParser
{
    /*
   public String toString()
    {
        JsonArray jsonArray = new JsonArray();

        for(ProjectVersion version: versionIndexDict.values())
        {
            jsonArray.add(version.getJsonObject());
        }

        return StringHandler.cleanUpRegex(jsonArray.toString(), Arrays.asList("\""));
    }
    */

    public static void parseOut(@NonNull ProjectLoader loader, @NonNull RowIterator<Row> rowIterator, @NonNull JsonObject jsonObject)
    {
        JsonObject content = new JsonObject();

        while(rowIterator.hasNext())
        {
            Row row = rowIterator.next();
            JsonArray item = new JsonArray().add(row.getString(2))      //annotation
                                            .add(row.getInteger(4))     //img_x
                                            .add(row.getInteger(5))     //img_y
                                            .add(row.getInteger(6))     //img_w
                                            .add(row.getInteger(7));    //img_h

            JsonObject versionBody = new JsonObject().put(loader.getCurrentProjectVersion().getVersionUuid(), item);

            JsonObject versionList = new JsonObject()
                    .put(ParamConfig.getImgPathParam(), row.getString(1))
                    .put(ParamConfig.getImgDepth(), row.getInteger(3))
                    .put(ParamConfig.getFileSizeParam(), row.getInteger(8))
                    .put(ParamConfig.getImgOriWParam(), row.getInteger(9))
                    .put(ParamConfig.getImgOriHParam(), row.getInteger(10))
                    .put(ParamConfig.getVersionListParam(), versionBody);

            //uuid, version, content
            content.put(row.getString(0), versionList);
        }

        jsonObject.put(ParamConfig.getProjectContentParam(), content);

    }


    public static void parseIn(@NonNull ProjectLoader loader, @NonNull JsonObject contentJsonBody)
    {
        String projectId = loader.getProjectID();

        String thisVersionUuid = loader.getCurrentProjectVersion().getVersionUuid();

        List<String> uuidListFromDb = loader.getUuidListFromDb();

        for(String uuid : uuidListFromDb)
        {
            JsonObject uuidBody = contentJsonBody.getJsonObject(uuid);

            JsonObject versionJsonBody = uuidBody.getJsonObject(ParamConfig.getVersionListParam());
            JsonArray currentVersionBody = versionJsonBody.getJsonArray(thisVersionUuid);

            JsonObject buffer = uuidBody.getJsonObject(ParamConfig.getVersionListParam());
            System.out.println("Version List Param: " + buffer);


            Tuple params = Tuple.of(uuid,                                        //uuid
                    projectId,                                             //project_id
                    uuidBody.getString(ParamConfig.getImgPathParam()),           //child_path
                    null,       //version_list
                    currentVersionBody.getString(0),                        //annotation
                    uuidBody.getInteger(ParamConfig.getImgDepth()),              //img_depth
                    currentVersionBody.getString(1),                        //img_X
                    currentVersionBody.getString(2),                        //img_Y
                    currentVersionBody.getString(3),                        //img_W
                    currentVersionBody.getString(4),                        //img_H
                    uuidBody.getInteger(ParamConfig.getFileSizeParam()),         //file_size
                    uuidBody.getInteger(ParamConfig.getImgOriWParam()),          //img_ori_w
                    uuidBody.getInteger(ParamConfig.getImgOriHParam()));         //img_ori_w


            AnnotationVerticle.writeUuidToDbFromImportingConfigFile(params, loader);
        }

    }
}
