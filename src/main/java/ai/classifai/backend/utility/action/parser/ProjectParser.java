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
package ai.classifai.backend.utility.action.parser;

import ai.classifai.backend.utility.Hash;
import ai.classifai.backend.loader.ProjectLoader;
import ai.classifai.backend.utility.handler.StringHandler;
import ai.classifai.core.properties.DataInfoProperties;
import ai.classifai.core.properties.ImageDataProperties;
import ai.classifai.core.properties.VersionConfigProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        ObjectMapper mp = new ObjectMapper();

        try {
            return mp.readValue(versionListString, new TypeReference<List<VersionConfigProperties>>() {});
        } catch (JsonProcessingException e) {
            log.info("Convert to object fail\n" + e);
            return Collections.emptyList();
        }
    }

    public static void parseIn(@NonNull ProjectLoader loader, @NonNull Map<String, ImageDataProperties> content)
    {
        String projectId = loader.getProjectId();

        for (Map.Entry<String, ImageDataProperties> item: content.entrySet()) {
            String uuid = item.getKey();

            ImageDataProperties imageProps = item.getValue();

            String subPath = String.join(File.separator, imageProps.getImgPath().split("[/\\\\]"));

            File fullPath = Paths.get(loader.getProjectPath().getAbsolutePath(), subPath).toFile();

            // Only proceed to uploading image if image exists. Else skip
            if(fullPath.exists()) {
                String currentHash = Hash.getHash256String(fullPath);
                String fileHash = imageProps.getChecksum();

                if (fileHash.equals(currentHash)) {
//                    Annotation annotation = Annotation.builder()
//                            .uuid(uuid)
//                            .projectId(projectId)
//                            .imgPath(subPath)
//                            .annotationDict(buildAnnotationDict(imageProps.getVersionList()))
//                            .imgDepth(imageProps.getImgDepth())
//                            .imgOriW(imageProps.getImgOriW())
//                            .imgOriH(imageProps.getImgOriH())
//                            .fileSize(imageProps.getFileSize())
//                            .build();

//                    loader.getUuidAnnotationDict().put(uuid, annotation);

//                    annotationDB.uploadUuidFromConfigFile(annotation.getTuple(), loader);
                } else {
                    log.debug("Hash not same for " + fullPath);
                }
            }
        }
    }

    //version of a project <> DataInfoProperties
//    public static Map<String, DataInfoProperties> buildAnnotationDict(String jsonVersionList)
//    {
//        ObjectMapper mp = new ObjectMapper();

//        try {
//            List<VersionConfigProperties> versionConfigProperties = mp.readValue(jsonVersionList, new TypeReference<>() {});
//            return buildAnnotationDict(versionConfigProperties);
//        } catch (JsonProcessingException e) {
//            log.info("Converting to annotationDict fail", e);
//            return Map.of();
//        }
//    }

    //version of a project <> DataInfoProperties
//    public static Map<String, DataInfoProperties> buildAnnotationDict(List<VersionConfigProperties> versionList)
//    {
//        return versionList.stream().collect(Collectors.toMap(
//                VersionConfigProperties::getVersionUuid,
//                VersionConfigProperties::getAnnotationData
//        ));
//    }

    //build empty annotationDict
    public static Map<String, DataInfoProperties> buildAnnotationDict(@NonNull ProjectLoader loader)
    {
        Map<String, DataInfoProperties> annotationDict = new HashMap<>();

        Set<String> versionUuidList = loader.getProjectVersion().getVersionUuidDict().keySet();

        for(String versionUuid : versionUuidList)
        {
            annotationDict.put(versionUuid, DataInfoProperties.builder().build());
        }

        return annotationDict;
    }

}
