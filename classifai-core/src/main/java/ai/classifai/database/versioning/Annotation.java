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
package ai.classifai.database.versioning;

import ai.classifai.action.ActionOps;
import ai.classifai.util.collection.UuidGenerator;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Tuple;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;


/**
 * Annotation Clob in Project Table
 *
 * @author codenamewei
 */
@Getter
@Setter
@Builder
public class Annotation
{
    @Builder.Default private String uuid = UuidGenerator.generateUuid();    //uuid
    private String projectId;                                               //project_id
    private String imgPath;                                                 //img_path

    //project version uuid <> annotation
    private Map<String, AnnotationVersion> annotationDict;                  //version_list

    @Builder.Default private Integer imgDepth = 0;                          //img_depth
    @Builder.Default private Integer fileSize = 0;                          //file_size

    @Builder.Default private Integer imgOriW = 0;                           //img_ori_w
    @Builder.Default private Integer imgOriH = 0;                           //img_ori_h

    public String getAnnotationDictDbFormat()
    {
        JsonArray response = new JsonArray();

        for (Map.Entry<String, AnnotationVersion> entry : annotationDict.entrySet())
        {
            String annotation = entry.getValue().getDbFormat();

            String strAnnotationVersion = ActionOps.removeDoubleQuote(new JsonObject().put(entry.getKey(), annotation).encode());

            response.add(strAnnotationVersion);
        }

        return ActionOps.removeDoubleQuote(response.encode());
    }

    public Tuple getTuple()
    {
        return Tuple.of(uuid,                            //uuid
                projectId,                               //project_id
                imgPath,                                 //img_path
                getAnnotationDictDbFormat(),             //version_list
                imgDepth,                                //img_depth
                fileSize,                                //file_size
                imgOriW,                                 //img_ori_w
                imgOriH);                                //img_ori_w
    }

}
