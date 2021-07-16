package ai.classifai.db.repository;

import ai.classifai.core.entities.Version;
import ai.classifai.core.entities.dto.VersionDTO;
import ai.classifai.core.services.repository.VersionRepository;
import ai.classifai.db.entities.VersionEntity;
import lombok.NonNull;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.UUID;

public class JPAVersionRepository extends JPARepository<Version, VersionDTO, UUID> implements VersionRepository
{
    public JPAVersionRepository(EntityManager em)
    {
        super(em, VersionEntity::fromDTO);
    }

    @Override
    public Version get(@NonNull UUID id) {
        return null;
    }

    @Override
    public List<? extends Version> list() {
        return null;
    }

    @Override
    public Version updateModifiedAt(Version version) {
        return null;
    }
}
