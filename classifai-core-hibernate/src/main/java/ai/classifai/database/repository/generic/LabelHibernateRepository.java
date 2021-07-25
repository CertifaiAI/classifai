package ai.classifai.database.repository.generic;

import ai.classifai.core.entity.dto.generic.LabelDTO;
import ai.classifai.core.entity.model.generic.Label;
import ai.classifai.core.entity.model.generic.Version;
import ai.classifai.core.service.generic.LabelRepository;
import ai.classifai.database.entity.generic.LabelEntity;
import ai.classifai.database.entity.generic.VersionEntity;
import lombok.NonNull;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LabelHibernateRepository extends AbstractHibernateRepository<Label, LabelDTO, UUID, LabelEntity> implements LabelRepository
{
    public LabelHibernateRepository(EntityManager entityManager)
    {
        super(entityManager, LabelEntity.class);
    }

    @Override
    public Label create(@NonNull LabelDTO labelDTO)
    {
        LabelEntity entity = new LabelEntity();
        entity.fromDTO(labelDTO);

        VersionEntity versionEntity = em.getReference(VersionEntity.class, labelDTO.getVersionId());
        versionEntity.addLabel(entity);

        em.persist(entity);
        return entity;
    }

    public Label update(@NonNull Label label, @NonNull LabelDTO labelDTO)
    {
        return null;
    }

    @Override
    public List<Label> listByVersion(Version version)
    {
        return new ArrayList<>(em.createNamedQuery("Label.listByVersion", LabelEntity.class)
                .setParameter("version", version)
                .getResultList());
    }
}
