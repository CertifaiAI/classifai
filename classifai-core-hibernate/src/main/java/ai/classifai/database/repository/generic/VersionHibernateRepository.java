package ai.classifai.database.repository.generic;

import ai.classifai.core.entity.model.generic.Project;
import ai.classifai.core.entity.model.generic.Version;
import ai.classifai.core.entity.dto.generic.VersionDTO;
import ai.classifai.core.service.generic.VersionRepository;
import ai.classifai.database.entity.generic.ProjectEntity;
import ai.classifai.database.entity.generic.VersionEntity;
import lombok.NonNull;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


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

    @Override
    public void deleteList(List<Version> versionList)
    {
        versionList.forEach(this::delete);
    }
}
