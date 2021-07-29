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
package ai.classifai.core.service.generic;

import ai.classifai.core.entity.dto.generic.VersionDTO;
import ai.classifai.core.entity.model.generic.Project;
import ai.classifai.core.entity.model.generic.Version;

import java.util.List;
import java.util.UUID;

/**
 * Repository of Version entity
 *
 * @author YinChuangSum
 */
public interface VersionRepository extends Repository<Version, VersionDTO, UUID>
{
    Version updateModifiedAt(Version version);

    Version resetCreatedAt(Version version);

    List<Version> listByProject(Project project);
}
