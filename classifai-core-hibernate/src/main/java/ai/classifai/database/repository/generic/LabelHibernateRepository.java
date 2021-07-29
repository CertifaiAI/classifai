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

import ai.classifai.core.entity.dto.generic.LabelDTO;
import ai.classifai.core.entity.model.generic.Label;
import ai.classifai.core.entity.model.generic.Version;
import ai.classifai.core.service.generic.LabelRepository;
import ai.classifai.database.entity.generic.LabelEntity;
import ai.classifai.database.entity.generic.VersionEntity;
import lombok.NonNull;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.UUID;

/**
 * Class for Label repository with hibernate implementation
 *
 * @author YinChuangSum
 */
public class LabelHibernateRepository extends AbstractHibernateRepository<Label, LabelDTO, UUID, LabelEntity> implements LabelRepository
{
    public LabelHibernateRepository(EntityManager entityManager)
    {
        super(entityManager, LabelEntity.class);
    }

    @Override
    public Label create(@NonNull LabelDTO dto)
    {
        LabelEntity entity = new LabelEntity();
        entity.fromDTO(dto);

        VersionEntity versionEntity = em.getReference(VersionEntity.class, dto.getVersionId());
        versionEntity.addLabel(entity);

        em.persist(entity);
        return entity;
    }

    public Label update(@NonNull Label label, @NonNull LabelDTO dto)
    {
        label.update(dto);
        return em.merge(label);
    }


    @Override
    public List<Label> listByVersion(Version version)
    {
        VersionEntity versionEntity = em.getReference(VersionEntity.class, version.getId());
        return versionEntity.getLabelList();
    }
}
