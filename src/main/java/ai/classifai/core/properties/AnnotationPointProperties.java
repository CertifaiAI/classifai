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
package ai.classifai.core.properties;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NonNull
public class AnnotationPointProperties {
    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    List<CoordinatesPointProperties> coorPt;

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Integer x1;

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Integer y1;

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Integer x2;

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Integer y2;

    @JsonProperty
    Integer lineWidth;

    @JsonProperty
    String color;

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String region;

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    List<String> subLabel;

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    DistanceToImageProperties distancetoImg;

    @JsonProperty
    String label;

    @JsonProperty
    String id;
}