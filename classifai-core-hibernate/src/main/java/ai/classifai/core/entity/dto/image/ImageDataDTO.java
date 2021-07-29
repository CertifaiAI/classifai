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
package ai.classifai.core.entity.dto.image;

import ai.classifai.core.entity.dto.generic.DataDTO;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * DTO class representing ImageData entity
 *
 * @author YinChuangSum
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ImageDataDTO extends DataDTO
{
    Integer depth;
    Integer height;
    Integer width;

    public static ImageDataDTO toDTOImpl(DataDTO dto)
    {
        if (dto instanceof ImageDataDTO)
        {
            return (ImageDataDTO) dto;
        }

        throw new IllegalArgumentException(String.format("%s is expected to be parsed but got %s", ImageDataDTO.class, dto.getClass()));
    }
}
