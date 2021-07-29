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
import ai.classifai.core.entity.dto.generic.LabelDTO;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * class for handling project loading response
 *
 * @author YinChuangSum
 */
public class ProjectLoadingView
{
    public JsonObject generateLoadProjectView(List<LabelDTO> labelDTOList, List<DataDTO> dataDTOList)
    {
        List<String> labelNameList = labelDTOList.stream()
                .map(LabelDTO::getName)
                .collect(Collectors.toList());

        List<String> dataIdList = dataDTOList.stream()
                .map(DataDTO::getId)
                .map(Objects::toString)
                .collect(Collectors.toList());

        return new JsonObject().put("message", 1)
                .put("label_list", new JsonArray(labelNameList))
                .put("uuid_list", new JsonArray(dataIdList));
    }

    // FIXME: to be deleted
    public JsonObject generateLoadProjectViewStatus(List<LabelDTO> labelDTOList, List<DataDTO> dataDTOList)
    {
        return generateLoadProjectView(labelDTOList, dataDTOList)
                .put("message", 2);
    }
}
