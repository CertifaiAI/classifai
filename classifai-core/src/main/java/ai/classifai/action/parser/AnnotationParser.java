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

import ai.classifai.loader.ProjectLoader;
import ai.classifai.util.ParamConfig;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/***
 * Parsing Project Table in and out classifai with configuration file
 *
 * @author codenamewei
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AnnotationParser
{
    public static void parseOut(@NonNull RowIterator<Row> rowIterator, @NonNull JsonObject jsonObject)
    {
        JsonObject content = new JsonObject();

        while(rowIterator.hasNext())
        {
            Row row = rowIterator.next();
            JsonObject item = new JsonObject();

            item.put(ParamConfig.getUuidParam(), row.getString(0));
            item.put(ParamConfig.getImgPathParam(), row.getString(2));
            item.put(ParamConfig.getAnnotationParam(), row.getString(3));

            item.put(ParamConfig.getImgDepth(), row.getInteger(4));
            item.put(ParamConfig.getImgXParam(), row.getInteger(5));
            item.put(ParamConfig.getImgYParam(), row.getInteger(6));

            item.put(ParamConfig.getImgWParam(), row.getInteger(7));
            item.put(ParamConfig.getImgHParam(), row.getInteger(8));
            item.put(ParamConfig.getFileSizeParam(), row.getInteger(9));

            item.put(ParamConfig.getImgOriWParam(), row.getInteger(10));
            item.put(ParamConfig.getImgOriHParam(), row.getInteger(11));

            content.put(row.getString(0), item);
        }

        jsonObject.put(ParamConfig.getProjectContentParam(), content);

    }


    public static void parseIn(@NonNull ProjectLoader loader, @NonNull JsonObject jsonObject)
    {
        String projectId = loader.getProjectID();



    }
}
