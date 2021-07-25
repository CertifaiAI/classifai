package ai.classifai.database.repository.image;

import ai.classifai.core.entity.dto.image.ImageDataDTO;
import ai.classifai.core.entity.dto.image.ImageDataVersionDTO;
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

public class ImageDataVersionHibernateRepository extends DataVersionHibernateRepository<ImageDataVersion, ImageDataVersionDTO, ImageDataVersionEntity, ImageData, ImageDataDTO> implements ImageDataVersionRepository
{
    public ImageDataVersionHibernateRepository(EntityManager em)
    {
        super(em, ImageDataVersionEntity.class);
    }

    @Override
    public ImageDataVersion create(@NonNull ImageDataVersionDTO dto)
    {
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

    public ImageDataVersion update(@NonNull ImageDataVersion imageDataVersion, @NonNull ImageDataVersionDTO imageDataVersionDTO) {
        return null;
    }

    @Override
    public List<ImageDataVersion> updateList(List<ImageDataVersion> dataVersionList, List<ImageDataVersionDTO> imageDataVersionDTOS) {
        return null;
    }

    @Override
    public List<ImageDataVersion> listByVersion(Version version) {
        return null;
    }

    @Override
    public List<ImageDataVersion> listByData(ImageData data) {
        return null;
    }

    @Override
    public ImageDataVersion getByDataAndVersion(ImageData data, Version version) {
        return null;
    }
}
