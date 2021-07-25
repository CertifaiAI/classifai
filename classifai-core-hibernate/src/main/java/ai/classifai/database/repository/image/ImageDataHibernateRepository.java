package ai.classifai.database.repository.image;

import ai.classifai.core.entity.dto.image.ImageDataDTO;
import ai.classifai.core.entity.model.image.ImageData;
import ai.classifai.core.service.image.ImageDataRepository;
import ai.classifai.database.entity.generic.ProjectEntity;
import ai.classifai.database.entity.image.ImageDataEntity;
import ai.classifai.database.repository.generic.DataHibernateRepository;
import lombok.NonNull;

import javax.persistence.EntityManager;

public class ImageDataHibernateRepository extends DataHibernateRepository<ImageData, ImageDataDTO, ImageDataEntity> implements ImageDataRepository
{
    public ImageDataHibernateRepository(EntityManager em) {
        super(em, ImageDataEntity.class);
    }

    @Override
    public ImageData create(@NonNull ImageDataDTO dto)
    {
        ImageDataEntity entity = new ImageDataEntity();
        entity.fromDTO(dto);

        ProjectEntity projectEntity = em.getReference(ProjectEntity.class, dto.getProjectId());
        projectEntity.addData(entity);

        em.persist(entity);
        return entity;
    }

    public ImageData update(@NonNull ImageData imageData, @NonNull ImageDataDTO imageDataDTO) {
        return null;
    }
}
