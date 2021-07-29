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

import ai.classifai.core.entity.dto.generic.VersionDTO;
import ai.classifai.core.entity.model.generic.Project;
import ai.classifai.core.entity.model.generic.Version;
import ai.classifai.core.service.generic.VersionRepository;
import ai.classifai.database.entity.generic.ProjectEntity;
import ai.classifai.database.entity.generic.VersionEntity;
import lombok.NonNull;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Class for Version repository with hibernate implementation
 *
 * @author YinChuangSum
 */
public class VersionHibernateRepository extends AbstractHibernateRepository<Version, VersionDTO, UUID, VersionEntity> implements VersionRepository
{
    public VersionHibernateRepository(EntityManager em)
    {
        super(em, VersionEntity.class);
    }

    @Override
    public Version create(@NonNull VersionDTO dto)
    {
        VersionEntity entity = new VersionEntity();

        entity.fromDTO(dto);
        ProjectEntity projectEntity = em.getReference(ProjectEntity.class, dto.getProjectId());

        projectEntity.addVersion(entity);

        em.persist(entity);
        return entity;
    }

    @Override
    public Version updateModifiedAt(Version version)
    {
        VersionEntity entity = toEntityImpl(version);
        entity.setModifiedAt(LocalDateTime.now());
        return em.merge(entity);
    }

    @Override
    public Version resetCreatedAt(Version version)
    {
        VersionEntity entity = toEntityImpl(version);
        entity.setCreatedAt(LocalDateTime.now());
        return em.merge(entity);
    }

    @Override
    public List<Version> listByProject(Project project)
    {
        return new ArrayList<>(project.getVersionList());
    }
}
