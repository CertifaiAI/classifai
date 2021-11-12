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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NonNull
public class ImageDataProperties {
    @JsonProperty
    String checksum;

    @JsonProperty("img_path")
    String imgPath;

    @JsonProperty("version_list")
    List<VersionConfigProperties> versionList;

    @JsonProperty("img_depth")
    Integer imgDepth;

    @JsonProperty("img_ori_w")
    Integer imgOriW;

    @JsonProperty("img_ori_h")
    Integer imgOriH;

    @JsonProperty("file_size")
    Integer fileSize;
}
