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
package ai.classifai.database.repository.image.annotation;

import ai.classifai.core.entity.dto.generic.AnnotationDTO;
import ai.classifai.core.entity.dto.image.annotation.BoundingBoxAnnotationDTO;
import ai.classifai.core.entity.dto.image.annotation.PolygonAnnotationDTO;
import ai.classifai.core.entity.model.generic.Annotation;
import ai.classifai.core.entity.model.image.annotation.PolygonAnnotation;
import ai.classifai.core.service.image.annotation.PolygonAnnotationRepository;
import ai.classifai.database.entity.image.annotation.BoundingBoxAnnotationEntity;
import ai.classifai.database.entity.image.annotation.ImageAnnotationEntity;
import ai.classifai.database.entity.image.annotation.PolygonAnnotationEntity;
import lombok.NonNull;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * Class for PolygonAnnotation repository with hibernate implementation
 *
 * @author YinChuangSum
 */
public class PolygonAnnotationHibernateRepository extends ImageAnnotationHibernateRepository implements PolygonAnnotationRepository
{

    public PolygonAnnotationHibernateRepository(EntityManager em) {
        super(em, PolygonAnnotationEntity.class);
    }

    @Override
    public Annotation create(@NonNull AnnotationDTO annotationDTO)
    {
        PolygonAnnotationDTO dto = PolygonAnnotationDTO.toDTOImpl(annotationDTO);
        PolygonAnnotationEntity entity = new PolygonAnnotationEntity();
        entity.fromDTO(dto);

        em.persist(entity);
        return entity;
    }
}
