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
package ai.classifai.core.properties.image;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NonNull
public class DataInfoProperties {

    @Builder.Default
    @JsonProperty("annotation")
    List<AnnotationPointProperties> annotation = new ArrayList<>();

    @Builder.Default
    @JsonProperty("img_x")
    int imgX = 0;

    @Builder.Default
    @JsonProperty("img_y")
    int imgY = 0;

    @Builder.Default
    @JsonProperty("img_w")
    int imgW = 0;

    @Builder.Default
    @JsonProperty("img_h")
    int imgH = 0;
}
