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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * DTO class representing Data entity
 *
 * @author YinChuangSum
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class DataDTO
{
    @Builder.Default
    UUID id = null;

    String path;
    String checksum;
    Long fileSize;

    @Builder.Default
    UUID projectId = null;

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof DataDTO)
        {
            DataDTO dto = (DataDTO) obj;
            return path.equals(dto.getPath()) && checksum.equals(dto.getChecksum());
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        return checksum.hashCode() + path.hashCode();
    }
}
