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
package ai.classifai.database.repository.image;

import ai.classifai.core.entity.dto.generic.DataDTO;
import ai.classifai.core.entity.dto.image.ImageDataDTO;
import ai.classifai.core.entity.model.generic.Data;
import ai.classifai.core.service.image.ImageDataRepository;
import ai.classifai.database.entity.generic.ProjectEntity;
import ai.classifai.database.entity.image.ImageDataEntity;
import ai.classifai.database.repository.generic.DataHibernateRepository;
import lombok.NonNull;

import javax.persistence.EntityManager;

/**
 * Class for ImageData repository with hibernate implementation
 *
 * @author YinChuangSum
 */
public class ImageDataHibernateRepository extends DataHibernateRepository implements ImageDataRepository
{
    public ImageDataHibernateRepository(EntityManager em)
    {
        super(em, ImageDataEntity.class);
    }

    @Override
    public Data create(@NonNull DataDTO dataDTO)
    {
        ImageDataDTO dto = ImageDataDTO.toDTOImpl(dataDTO);
        ImageDataEntity entity = new ImageDataEntity();
        entity.fromDTO(dto);

        ProjectEntity projectEntity = em.getReference(ProjectEntity.class, dto.getProjectId());
        projectEntity.addData(entity);

        em.persist(entity);
        return entity;
    }
}
