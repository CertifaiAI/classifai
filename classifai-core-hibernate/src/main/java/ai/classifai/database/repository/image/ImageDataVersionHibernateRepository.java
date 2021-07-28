package ai.classifai.database.repository.image;

import ai.classifai.core.entity.dto.generic.DataVersionDTO;
import ai.classifai.core.entity.dto.image.ImageDataDTO;
import ai.classifai.core.entity.dto.image.ImageDataVersionDTO;
import ai.classifai.core.entity.model.generic.DataVersion;
import ai.classifai.core.entity.model.generic.Version;
import ai.classifai.core.entity.model.image.ImageData;
import ai.classifai.core.entity.model.image.ImageDataVersion;
import ai.classifai.core.service.image.ImageDataVersionRepository;
import ai.classifai.database.entity.generic.VersionEntity;
import ai.classifai.database.entity.generic.DataVersionKey;
import ai.classifai.database.entity.image.ImageDataEntity;
import ai.classifai.database.entity.image.ImageDataVersionEntity;
import ai.classifai.database.repository.generic.DataVersionHibernateRepository;
import lombok.NonNull;

import javax.persistence.EntityManager;
import java.util.List;

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
