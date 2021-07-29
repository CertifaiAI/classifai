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
package ai.classifai.core.entity.dto.generic;

import ai.classifai.util.project.ProjectInfra;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO class representing Project entity
 *
 * @author YinChuangSum
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDTO {
    @Builder.Default
    private UUID id = null;

    private String name;
    private Integer type;
    private String path;

    @Builder.Default
    private Boolean isNew = true;

    @Builder.Default
    private Boolean isStarred = false;

    @Builder.Default
    private Integer infra = ProjectInfra.ON_PREMISE.ordinal();

    @Builder.Default
    private UUID currentVersionId = null;

    @Builder.Default
    private List<UUID> versionIdList = new ArrayList<>();

    @Builder.Default
    private List<UUID> dataIdList = new ArrayList<>();
}
