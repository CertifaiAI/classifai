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

import ai.classifai.core.entity.dto.generic.DataDTO;
import ai.classifai.selector.status.FileSystemStatus;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.message.ReplyHandler;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.stream.Collectors;

/**
 * class for handling project reload response
 *
 * @author YinChuangSum
 */
public class ProjectReloadView
{
    public JsonObject generate(List<DataDTO> addedDataDTOList)
    {
        return new JsonObject()
                .put("uuid_add_list", getUuidStringList(addedDataDTOList));
    }

    public JsonObject generateStatus(List<DataDTO> addedDataDTOList)
    {
        JsonObject response = ReplyHandler.getOkReply();

        return response.put(ParamConfig.getFileSysStatusParam(), FileSystemStatus.DATABASE_UPDATED.ordinal())
                .put(ParamConfig.getFileSysMessageParam(), FileSystemStatus.DATABASE_UPDATED.name())
                .put("uuid_add_list", getUuidStringList(addedDataDTOList));
    }

    private List<String> getUuidStringList(List<DataDTO> addedDataDTOList) {
        return addedDataDTOList.stream()
                .map(dataDTO -> dataDTO.getId().toString())
                .collect(Collectors.toList());
    }
}
