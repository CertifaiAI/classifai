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

import ai.classifai.core.entity.dto.generic.PointDTO;
import ai.classifai.core.entity.model.generic.Point;
import ai.classifai.core.service.generic.PointRepository;
import ai.classifai.database.entity.generic.PointEntity;
import ai.classifai.database.entity.image.annotation.ImageAnnotationEntity;
import lombok.NonNull;

import javax.persistence.EntityManager;
import java.util.UUID;

/**
 * Class for Point repository with hibernate implementation
 *
 * @author YinChuangSum
 */
public class PointHibernateRepository extends AbstractHibernateRepository<Point, PointDTO, UUID, PointEntity> implements PointRepository
{
    public PointHibernateRepository(EntityManager em) {
        super(em, PointEntity.class);
    }

    @Override
    public Point create(@NonNull PointDTO dto)
    {
        PointEntity entity = new PointEntity();
        ImageAnnotationEntity imageAnnotationEntity = em.getReference(ImageAnnotationEntity.class, dto.getAnnotationId());

        entity.fromDTO(dto);
        imageAnnotationEntity.addPoint(entity);
        em.persist(entity);

        return entity;
    }

    public Point update(@NonNull Point point, @NonNull PointDTO pointDTO)
    {
        point.update(pointDTO);
        return em.merge(point);
    }
}
