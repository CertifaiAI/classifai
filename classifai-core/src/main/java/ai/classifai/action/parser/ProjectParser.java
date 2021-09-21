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
import ai.classifai.database.portfolio.PortfolioVerticle;
import ai.classifai.database.versioning.Annotation;
import ai.classifai.database.versioning.AnnotationVersion;
import ai.classifai.dto.DataInfoProperties;
import ai.classifai.dto.ImageDataProperties;
import ai.classifai.dto.AnnotationPointProperties;
import ai.classifai.dto.VersionConfigProperties;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.util.Hash;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.data.StringHandler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

/***
 * Parsing Project Table in and out classifai with configuration file
 *
 * @author codenamewei
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProjectParser
{
    public static Map<String, ImageDataProperties> parseOut(@NonNull String projectPath, @NonNull RowIterator<Row> rowIterator)
    {
        HashMap<String, ImageDataProperties> content = new HashMap<>();

        while(rowIterator.hasNext())
        {
            Row row = rowIterator.next();

            String imgPath = StringHandler.removeFirstSlashes(row.getString(1));

            String fullPath = Paths.get(projectPath, imgPath).toString();

            String hash = Hash.getHash256String(new File(fullPath));

            ImageDataProperties config = ImageDataProperties.builder()
                    .checksum(hash)
                    .imgPath(imgPath)
                    .versionList(getVersionList(row.getString(2)))
                    .imgDepth(row.getInteger(3))
                    .imgOriW(row.getInteger(4))
                    .imgOriH(row.getInteger(5))
                    .fileSize(row.getInteger(6))
                    .build();

            //uuid, version, content
            content.put(row.getString(0), config);
        }

        return content;

    }

    private static List<VersionConfigProperties> getVersionList(String versionListString) {
        JsonArray versionListArray = new JsonArray(versionListString);

        List<VersionConfigProperties> config = new ArrayList<>();

        for(int i=0; i < versionListArray.size(); ++i) {
            JsonObject jsonAnnotation = versionListArray.getJsonObject(i);
            JsonObject annotationConfig = jsonAnnotation.getJsonObject(ParamConfig.getAnnotationDataParam());
            JsonArray annotationConfigArray = annotationConfig.getJsonArray(ParamConfig.getAnnotationParam());
            List<AnnotationPointProperties> annotationPoints = new ArrayList<>();
            for(int j=0; j < annotationConfigArray.size(); ++j) {
                JsonObject jsonAnnotationConfig = annotationConfigArray.getJsonObject(j);
                annotationPoints.add(PortfolioVerticle.getAnnotations(jsonAnnotationConfig));
            }

            DataInfoProperties annotationData = DataInfoProperties.builder()
                    .annotation(annotationPoints)
                    .imgX(annotationConfig.getInteger(ParamConfig.getImgXParam()))
                    .imgY(annotationConfig.getInteger(ParamConfig.getImgYParam()))
                    .imgW(annotationConfig.getInteger(ParamConfig.getImgWParam()))
                    .imgH(annotationConfig.getInteger(ParamConfig.getImgHParam()))
                    .build();

            VersionConfigProperties versionConfig = VersionConfigProperties.builder()
                    .versionUuid(jsonAnnotation.getString(ParamConfig.getVersionUuidParam()))
                    .annotationData(annotationData)
                    .build();

            config.add(versionConfig);
        }

        return config;
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

            String subPath = String.join(File.separator,jsonObject.getString(ParamConfig.getImgPathParam()).split("[/\\\\]"));

            File fullPath = Paths.get(loader.getProjectPath().getAbsolutePath(), subPath).toFile();

            // Only proceed to uploading image if image exists. Else skip
            if(fullPath.exists())
            {
                String currentHash = Hash.getHash256String(fullPath);

                String fileHash = jsonObject.getString(ParamConfig.getCheckSumParam());

                if(fileHash.equals(currentHash))
                {
                    Annotation annotation = Annotation.builder()
                            .uuid(uuid)
                            .projectId(projectId)
                            .imgPath(subPath)
                            .annotationDict(buildAnnotationDict(jsonObject.getJsonArray(ParamConfig.getVersionListParam())))
                            .imgDepth(jsonObject.getInteger(ParamConfig.getImgDepth()))
                            .imgOriW(jsonObject.getInteger(ParamConfig.getImgOriWParam()))
                            .imgOriH(jsonObject.getInteger(ParamConfig.getImgOriHParam()))
                            .fileSize(jsonObject.getInteger(ParamConfig.getFileSizeParam()))
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
    }

    //version of a project <> AnnotationVersion
    public static Map<String, AnnotationVersion> buildAnnotationDict(JsonArray jsonVersionList)
    {
        Map<String, AnnotationVersion> annotationDict = new HashMap<>();

        for (Object obj : jsonVersionList)
        {
            JsonObject jsonVersion = (JsonObject) obj;

            String version = jsonVersion.getString(ParamConfig.getVersionUuidParam());

            AnnotationVersion annotationVersion = new AnnotationVersion((jsonVersion.getJsonObject(ParamConfig.getAnnotationDataParam())));

            annotationDict.put(version, annotationVersion);
        }

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
