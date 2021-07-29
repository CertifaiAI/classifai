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

import ai.classifai.core.entity.dto.generic.DataVersionDTO;
import ai.classifai.core.entity.dto.image.ImageDataVersionDTO;
import ai.classifai.core.entity.model.image.ImageDataVersion;
import ai.classifai.core.service.image.ImageDataVersionRepository;
import ai.classifai.database.entity.generic.DataVersionKey;
import ai.classifai.database.entity.generic.VersionEntity;
import ai.classifai.database.entity.image.ImageDataEntity;
import ai.classifai.database.entity.image.ImageDataVersionEntity;
import ai.classifai.database.repository.generic.DataVersionHibernateRepository;
import lombok.NonNull;

import javax.persistence.EntityManager;

/**
 * Class for ImageDataVersion repository with hibernate implementation
 *
 * @author YinChuangSum
 */
public class ImageDataVersionHibernateRepository extends DataVersionHibernateRepository implements ImageDataVersionRepository
{
    public ImageDataVersionHibernateRepository(EntityManager em)
    {
        super(em, ImageDataVersionEntity.class);
    }

    @Override
    public ImageDataVersion create(@NonNull DataVersionDTO dataVersionDTO)
    {
        ImageDataVersionDTO dto = ImageDataVersionDTO.toDTOImpl(dataVersionDTO);
        ImageDataVersionEntity entity = new ImageDataVersionEntity();
        entity.fromDTO(dto);

        ImageDataEntity dataEntity = em.getReference(ImageDataEntity.class, dto.getDataId());
        VersionEntity versionEntity = em.getReference(VersionEntity.class, dto.getVersionId());

        dataEntity.addDataVersion(entity);
        versionEntity.addDataVersion(entity);
        entity.setId(new DataVersionKey(dto.getDataId(), dto.getVersionId()));

        em.persist(entity);
        return entity;
    }
}
