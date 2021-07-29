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
package ai.classifai.core.service.image;

import ai.classifai.core.entity.dto.generic.DataVersionDTO;
import ai.classifai.core.entity.model.generic.DataVersion;
import ai.classifai.core.service.generic.DataVersionRepository;
import ai.classifai.core.service.generic.Repository;

/**
 * Repository ImageDataVersion of Annotation entity
 *
 * @author YinChuangSum
 */
public interface ImageDataVersionRepository extends DataVersionRepository, Repository<DataVersion, DataVersionDTO, DataVersion.DataVersionId>
{}
