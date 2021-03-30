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

import ai.classifai.action.ActionOps;
import ai.classifai.database.annotation.AnnotationVerticle;
import ai.classifai.database.versioning.Annotation;
import ai.classifai.database.versioning.AnnotationVersion;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.util.Hash;
import ai.classifai.util.ParamConfig;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/***
 * Parsing Project Table in and out classifai with configuration file
 *
 * @author codenamewei
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProjectParser
{
    public static void parseOut(@NonNull String projectPath, @NonNull RowIterator<Row> rowIterator, @NonNull JsonObject jsonObject)
    {
        JsonObject content = new JsonObject();

        while(rowIterator.hasNext())
        {
            Row row = rowIterator.next();

            String imgPath = row.getString(1).replace("\\\\", "").replace("\\\\", "");

            String fullPath = Paths.get(projectPath, imgPath).toString();

            String hash = Hash.getHash256String(new File(fullPath));

            JsonObject annotationJsonObject = new JsonObject()
                    .put(ParamConfig.getCheckSumParam(), hash)
                    .put(ParamConfig.getImgPathParam(), imgPath)       //img_path
                    .put(ParamConfig.getVersionListParam(), row.getString(2))   //version_list
                    .put(ParamConfig.getImgDepth(), row.getInteger(3))          //img_depth
                    .put(ParamConfig.getImgOriWParam(), row.getInteger(4))      //img_ori_w
                    .put(ParamConfig.getImgOriHParam(), row.getInteger(5));     //img_ori_h


            //uuid, version, content
            content.put(row.getString(0), annotationJsonObject);
        }

        jsonObject.put(ParamConfig.getProjectContentParam(), content);

    }

    public static void parseIn(@NonNull ProjectLoader loader, @NonNull JsonObject contentJsonBody)
    {
        String projectId = loader.getProjectId();

        Iterator<Map.Entry<String, Object>> iterator = contentJsonBody.iterator();

        while(iterator.hasNext())
        {
            Map.Entry<String, Object> item = iterator.next();
            String uuid = item.getKey();

            JsonObject jsonObject = (JsonObject) item.getValue();

            String subPath = jsonObject.getString(ParamConfig.getImgPathParam());

            String fullPath = Paths.get(loader.getProjectPath(), subPath).toString();

            String currentHash = Hash.getHash256String(new File(fullPath));

            String fileHash = jsonObject.getString(ParamConfig.getCheckSumParam());

            if(fileHash.equals(currentHash))
            {
                String versionList = jsonObject.getString(ParamConfig.getVersionListParam());

                Annotation annotation = Annotation.builder()
                        .uuid(uuid)
                        .projectId(projectId)
                        .imgPath(subPath)
                        .annotationDict(buildAnnotationDict(versionList))
                        .imgDepth(jsonObject.getInteger(ParamConfig.getImgDepth()))
                        .imgOriW(jsonObject.getInteger(ParamConfig.getImgOriWParam()))
                        .imgOriH(jsonObject.getInteger(ParamConfig.getImgOriHParam()))
                        .build();

                loader.getUuidAnnotationDict().put(uuid, annotation);

                AnnotationVerticle.uploadUuidFromConfigFile(annotation.getTuple(), loader);
            }
            else
            {
                log.debug("Hash not same for " + fullPath);
            }
        }
    }

    //version of a project <> AnnotationVersion
    public static Map<String, AnnotationVersion> buildAnnotationDict(String strVersionList)
    {
        Map<String, AnnotationVersion> annotationDict = new HashMap<>();

        String thisVersionTrimmed = ActionOps.removeOuterBrackets(strVersionList);

        Integer separator = thisVersionTrimmed.indexOf(":");

        String version = thisVersionTrimmed.substring(0, separator);

        String strAnnotationVersion = thisVersionTrimmed.substring(separator + 1);

        AnnotationVersion annotationVersion = new AnnotationVersion(strAnnotationVersion);

        annotationDict.put(version, annotationVersion);

        return annotationDict;
    }

    //build empty annotationDict
    public static Map<String, AnnotationVersion> buildAnnotationDict(@NonNull ProjectLoader loader)
    {
        Map<String, AnnotationVersion> annotationDict = new HashMap<>();

        Set<String> versionUuidList = loader.getProjectVersion().getVersionUuidDict().keySet();

        for(String versionUuid : versionUuidList)
        {
            annotationDict.put(versionUuid, new AnnotationVersion());
        }

        return annotationDict;
    }

}
