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
package ai.classifai.database.repository.generic;

import ai.classifai.core.entity.dto.generic.AnnotationDTO;
import ai.classifai.core.entity.model.generic.Annotation;
import ai.classifai.core.entity.model.generic.Label;
import ai.classifai.core.service.generic.AnnotationRepository;
import ai.classifai.database.entity.generic.AnnotationEntity;
import ai.classifai.database.entity.generic.DataVersionEntity;
import ai.classifai.database.entity.generic.LabelEntity;
import lombok.NonNull;

import javax.persistence.EntityManager;

/**
 * Class for Annotation abstract repository with hibernate implementation
 *
 * @author YinChuangSum
 */
public class AnnotationHibernateRepository extends AbstractHibernateRepository<Annotation, AnnotationDTO, Long, AnnotationEntity> implements AnnotationRepository
{
    public AnnotationHibernateRepository(EntityManager em)
    {
        super(em, AnnotationEntity.class);
    }

    public AnnotationHibernateRepository(EntityManager em, Class<? extends AnnotationEntity> entityClass)
    {
        super(em, entityClass);
    }

    @Override
    public Annotation setLabel(Annotation annotation, Label label)
    {
        AnnotationEntity entity = toEntityImpl(annotation);
        LabelEntity labelEntity = em.getReference(LabelEntity.class, label.getId());
        entity.setLabel(labelEntity);
        return em.merge(entity);
    }

    @Override
    public void delete(@NonNull Annotation annotation)
    {
        AnnotationEntity entity = toEntityImpl(annotation);
        DataVersionEntity dataVersionEntity = em.getReference(DataVersionEntity.class, annotation.getDataVersion().getId());
        dataVersionEntity.removeAnnotation(entity);
        super.delete(entity);
    }
}
