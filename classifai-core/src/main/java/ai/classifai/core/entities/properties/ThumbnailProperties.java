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
package ai.classifai.core.entities.properties;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NonNull
public class ThumbnailProperties {

    @JsonProperty
    int message;

    @NonNull
    @JsonProperty("uuid")
    String uuidParam;

    @NonNull
    @JsonProperty("project_name")
    String projectNameParam;

    @NonNull
    @JsonProperty("img_path")
    String imgPathParam;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("polygons")
    List<AnnotationPointProperties> segmentationParam;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("bnd_box")
    List<AnnotationPointProperties> boundingBoxParam;

    @NonNull
    @JsonProperty("img_depth")
    Integer imgDepth;

    @NonNull
    @JsonProperty("img_x")
    Integer imgXParam;

    @NonNull
    @JsonProperty("img_y")
    Integer imgYParam;

    @NonNull
    @JsonProperty("img_w")
    Integer imgWParam;

    @NonNull
    @JsonProperty("img_h")
    Integer imgHParam;

    @NonNull
    @JsonProperty("file_size")
    Integer fileSizeParam;

    @NonNull
    @JsonProperty("img_ori_w")
    Integer imgOriWParam;

    @NonNull
    @JsonProperty("img_ori_h")
    Integer imgOriHParam;

    @NonNull
    @JsonProperty("img_thumbnail")
    String imgThumbnailParam;
}
