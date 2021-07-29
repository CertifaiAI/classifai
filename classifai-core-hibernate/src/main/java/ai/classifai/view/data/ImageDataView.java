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
package ai.classifai.view.data;

import ai.classifai.core.entity.dto.generic.ProjectDTO;
import ai.classifai.core.entity.dto.generic.AnnotationDTO;
import ai.classifai.core.entity.dto.generic.DataDTO;
import ai.classifai.core.entity.dto.generic.DataVersionDTO;
import ai.classifai.core.entity.dto.image.ImageDataDTO;
import ai.classifai.core.entity.dto.image.ImageDataVersionDTO;
import ai.classifai.view.data.DataView;
import ai.classifai.view.dataversion.DataVersionView;
import io.vertx.core.json.JsonObject;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * class for handling image data response
 *
 * @author YinChuangSum
 */
@NoArgsConstructor
public class ImageDataView extends DataView
{
    // FIXME: mixed data, dataversion
    public JsonObject generateImageDataView(ProjectDTO projectDTO, ImageDataDTO imageDataDTO, String thumbnail, JsonObject dataVersionView, JsonObject annotationView)
    {
        return new JsonObject()
                .put("uuid", imageDataDTO.getId().toString())
                .put("project_name", projectDTO.getName())
                .put("img_path", imageDataDTO.getPath())
                .put("img_depth", imageDataDTO.getDepth())
                .put("img_ori_w", imageDataDTO.getWidth())
                .put("img_ori_h", imageDataDTO.getHeight())
                .put("file_size", imageDataDTO.getFileSize())
                .put("img_thumbnail", thumbnail)
                .mergeIn(dataVersionView)
                .mergeIn(annotationView);
    }
}
