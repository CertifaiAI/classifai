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

import ai.classifai.core.entity.trait.HasId;
import lombok.NonNull;

/**
 * Abstract repository interface with methods read, update and delete. Create method is not included.
 * Implemented by repository of abstract entity such as Data, DataVersion, Annotation and etc.
 *
 * @author YinChuangSum
 */
public interface AbstractRepository <entity extends HasId<id>, dto, id>
{
    entity get(@NonNull id id);

//    entity update(@NonNull entity entity, @NonNull DTO dto);

    void delete(@NonNull entity entity);
}
