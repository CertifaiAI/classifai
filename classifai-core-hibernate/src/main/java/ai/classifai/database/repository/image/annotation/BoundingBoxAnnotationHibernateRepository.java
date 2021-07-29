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
import ai.classifai.core.entity.model.generic.Annotation;
import ai.classifai.core.service.image.annotation.BoundingBoxAnnotationRepository;
import ai.classifai.database.entity.generic.DataVersionEntity;
import ai.classifai.database.entity.generic.DataVersionKey;
import ai.classifai.database.entity.generic.LabelEntity;
import ai.classifai.database.entity.image.annotation.BoundingBoxAnnotationEntity;
import lombok.NonNull;

import javax.persistence.EntityManager;

/**
 * Class for BoundingBoxAnnotation repository with hibernate implementation
 *
 * @author YinChuangSum
 */
public class BoundingBoxAnnotationHibernateRepository extends ImageAnnotationHibernateRepository implements BoundingBoxAnnotationRepository
{
    public BoundingBoxAnnotationHibernateRepository(EntityManager em) {
        super(em, BoundingBoxAnnotationEntity.class);
    }

    @Override
    public Annotation create(@NonNull AnnotationDTO annotationDTO)
    {
        BoundingBoxAnnotationDTO dto = BoundingBoxAnnotationDTO.toDTOImpl(annotationDTO);
        BoundingBoxAnnotationEntity entity = new BoundingBoxAnnotationEntity();
        entity.fromDTO(dto);

        DataVersionEntity dataVersionEntity = em.getReference(DataVersionEntity.class, new DataVersionKey(dto.getDataId(), dto.getVersionId()));
        LabelEntity labelEntity = em.getReference(LabelEntity.class, dto.getLabelId());

        dataVersionEntity.addAnnotation(entity);
        entity.setLabel(labelEntity);

        em.persist(entity);
        return entity;
    }
}
