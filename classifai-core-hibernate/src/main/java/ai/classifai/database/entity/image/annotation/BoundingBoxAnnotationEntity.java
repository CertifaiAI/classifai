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
package ai.classifai.database.entity.image.annotation;

import ai.classifai.core.entity.dto.generic.AnnotationDTO;
import ai.classifai.core.entity.dto.image.annotation.BoundingBoxAnnotationDTO;
import ai.classifai.core.entity.model.image.annotation.BoundingBoxAnnotation;
import ai.classifai.core.entity.trait.HasId;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.stream.Collectors;

/**
 * Class for BoundingBoxAnnotation entity with hibernate implementation
 *
 * @author YinChuangSum
 */
@Getter
@Entity(name = "boundingbox_annotation")
public class BoundingBoxAnnotationEntity extends ImageAnnotationEntity implements BoundingBoxAnnotation
{
    public BoundingBoxAnnotationEntity()
    {
        super();
    }

    @Override
    public BoundingBoxAnnotationDTO toDTO()
    {
        return BoundingBoxAnnotationDTO.builder()
                .id(getId())
                .position(getPosition())
                .labelId(getLabel().getId())
                .dataId(getDataVersion().getId().getDataId())
                .versionId(getDataVersion().getId().getVersionId())
                .pointIdList(getPointList().stream()
                        .map(HasId::getId)
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public void fromDTO(AnnotationDTO annotationDTO)
    {
        BoundingBoxAnnotationDTO dto = BoundingBoxAnnotationDTO.toDTOImpl(annotationDTO);
        setId(dto.getId());
        update(dto);
    }

    @Override
    public void update(AnnotationDTO annotationDTO)
    {
        BoundingBoxAnnotationDTO dto = BoundingBoxAnnotationDTO.toDTOImpl(annotationDTO);
        setPosition(dto.getPosition());
    }
}
