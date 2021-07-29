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
package ai.classifai.core.entity.trait;

/**
 * Every entity interface must implement HasDTO with a DTO class
 *
 * @author YinChuangSum
 */
public interface HasDTO<DTO>
{
    DTO toDTO();

    // create a new instance => set all parameters
    void fromDTO(DTO dto);

    // update existing entity => set only certain parameters
    void update(DTO dto);
}
