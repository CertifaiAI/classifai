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

import ai.classifai.core.entity.dto.generic.DataDTO;
import ai.classifai.core.entity.dto.image.ImageDataDTO;
import ai.classifai.core.entity.model.generic.Project;
import ai.classifai.core.entity.dto.generic.ProjectDTO;
import ai.classifai.core.entity.model.generic.Version;
import ai.classifai.core.entity.model.generic.Data;
import ai.classifai.core.entity.model.image.ImageData;
import ai.classifai.core.service.generic.ProjectRepository;
import ai.classifai.database.entity.generic.ProjectEntity;
import ai.classifai.database.entity.generic.VersionEntity;
import ai.classifai.database.entity.generic.DataEntity;
import ai.classifai.database.entity.image.ImageDataEntity;
import lombok.NonNull;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Class for Project repository with hibernate implementation
 *
 * @author YinChuangSum
 */
public class ProjectHibernateRepository extends AbstractHibernateRepository<Project, ProjectDTO, UUID, ProjectEntity> implements ProjectRepository
{
    public ProjectHibernateRepository(EntityManager em)
    {
        super(em, ProjectEntity.class);
    }

    @Override
    public Optional<Project> findByNameAndType(@NonNull String name, @NonNull Integer type)
    {
        return em.createNamedQuery("Project.findByAnnotationAndName", ProjectEntity.class)
                .setParameter("type", type)
                .setParameter("name", name)
                .getResultList()
                .stream()
                .map(projectEntity -> (Project) projectEntity)
                .findFirst();
    }

    @Override
    public List<Project> listByType(@NonNull Integer type)
    {
        List<ProjectEntity> projectEntityList = em.createNamedQuery("Project.listByAnnotation", ProjectEntity.class)
                .setParameter("type", type)
                .getResultList();

        return new ArrayList<>(projectEntityList);
    }

    @Override
    public Project rename(Project project, @NonNull String newName)
    {
        ProjectEntity entity = toEntityImpl(project);
        entity.setName(newName);
        return em.merge(entity);
    }

    @Override
    public Project star(Project project, @NonNull Boolean isStarred)
    {
        ProjectEntity entity = toEntityImpl(project);
        entity.setIsStarred(isStarred);
         return em.merge(entity);
    }

    @Override
    public Project setNew(Project project, @NonNull Boolean isNew)
    {
        ProjectEntity entity = toEntityImpl(project);
        entity.setIsNew(isNew);
        return em.merge(entity);
    }

    @Override
    public Project setCurrentVersion(Project project, @NonNull Version version)
    {
        VersionEntity versionEntity = em.getReference(VersionEntity.class, version.getId());
        ProjectEntity entity = toEntityImpl(project);

        entity.setCurrentVersion(versionEntity);
        return em.merge(entity);
    }

    @Override
    public Project addVersion(Project project, @NonNull Version version)
    {
        VersionEntity versionEntity = em.getReference(VersionEntity.class, version.getId());
        ProjectEntity entity = toEntityImpl(project);

        entity.getVersionList().add(versionEntity);
        return em.merge(entity);
    }

    @Override
    public Project addData(Project project, @NonNull Data data)
    {
        DataEntity dataEntity = em.getReference(DataEntity.class, data.getId());
        ProjectEntity entity = toEntityImpl(project);

        entity.addData(dataEntity);

        return em.merge(entity);
    }

    @Override
    public Project addDataList(Project project, @NonNull List<? extends Data> dataList)
    {
        Project entity = project;

        for (Data data : dataList)
        {
            entity = addData(project, data);
        }

        return entity;
    }

    @Override
    public Project create(@NonNull ProjectDTO projectDTO)
    {
        ProjectEntity entity = new ProjectEntity();

        entity.fromDTO(projectDTO);
        em.persist(entity);

        return entity;
    }
}
