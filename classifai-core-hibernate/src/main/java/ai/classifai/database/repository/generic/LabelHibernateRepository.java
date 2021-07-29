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
