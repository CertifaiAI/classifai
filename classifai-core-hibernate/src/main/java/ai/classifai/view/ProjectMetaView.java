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
package ai.classifai.view;

import ai.classifai.core.entity.dto.generic.ProjectDTO;
import ai.classifai.core.entity.dto.generic.VersionDTO;
import ai.classifai.util.project.ProjectInfra;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

/**
 * class for handling project meta response
 *
 * @author YinChuangSum
 */
public class ProjectMetaView
{
    public JsonObject generateMetaList(@NonNull List<ProjectDTO> projectDTOList, @NonNull List<VersionDTO> versionDTOList,
                                              List<Boolean> pathValidList, List<Boolean> projectLoadedList)
    {
        JsonArray content = new JsonArray();

        IntStream.range(0, projectDTOList.size())
                .forEach(i -> content.add(generateSingleMeta(projectDTOList.get(i), versionDTOList.get(i),pathValidList.get(i), projectLoadedList.get(i))));

        return new JsonObject().put("message", 1)
                .put("content", content);
    }

    private JsonObject generateSingleMeta(ProjectDTO projectDTO, VersionDTO versionDTO, Boolean isPathValid, Boolean isProjectLoaded)
    {
        return new JsonObject()
                .put("project_name", projectDTO.getName())
                .put("project_path", projectDTO.getPath())
                .put("is_new", projectDTO.getIsNew())
                .put("is_starred", projectDTO.getIsStarred())
                .put("is_loaded", isProjectLoaded)
                .put("is_cloud", false)
                .put("project_infra", ProjectInfra.fromInt(projectDTO.getInfra()).name())
                .put("created_date", versionDTO.getCreatedAt().toString())
                .put("last_modified_date", versionDTO.getModifiedAt().toString())
                .put("current_version", versionDTO.getId().toString())
                .put("total_uuid", projectDTO.getDataIdList().size())
                .put("root_path_valid", isPathValid);
    }


    public JsonObject generateMeta(ProjectDTO projectDTO, VersionDTO versionDTO,
                                          Boolean isPathValid, Boolean isProjectLoaded)
    {
        List<ProjectDTO> projectDTOList = Collections.singletonList(projectDTO);
        List<VersionDTO> versionDTOList = Collections.singletonList(versionDTO);
        List<Boolean> pathValidList = Collections.singletonList(isPathValid);
        List<Boolean> projectLoadedList = Collections.singletonList(isProjectLoaded);

        return generateMetaList(projectDTOList, versionDTOList, pathValidList, projectLoadedList);
    }
}
