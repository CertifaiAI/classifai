package ai.classifai.database.repository.image;

import ai.classifai.core.entity.dto.generic.DataDTO;
import ai.classifai.core.entity.dto.image.ImageDataDTO;
import ai.classifai.core.entity.model.generic.Data;
import ai.classifai.core.entity.model.image.ImageData;
import ai.classifai.core.service.image.ImageDataRepository;
import ai.classifai.database.entity.generic.DataEntity;
import ai.classifai.database.entity.generic.ProjectEntity;
import ai.classifai.database.entity.image.ImageDataEntity;
import ai.classifai.database.repository.generic.DataHibernateRepository;
import lombok.NonNull;

import javax.persistence.EntityManager;

/**
 * Class for ImageData repository with hibernate implementation
 *
 * @author YinChuangSum
 */
public class ImageDataHibernateRepository extends DataHibernateRepository implements ImageDataRepository
{
    public ImageDataHibernateRepository(EntityManager em)
    {
        super(em, ImageDataEntity.class);
    }

    @Override
    public Data create(@NonNull DataDTO dataDTO)
    {
        ImageDataDTO dto = ImageDataDTO.toDTOImpl(dataDTO);
        ImageDataEntity entity = new ImageDataEntity();
        entity.fromDTO(dto);

        ProjectEntity projectEntity = em.getReference(ProjectEntity.class, dto.getProjectId());
        projectEntity.addData(entity);

        em.persist(entity);
        return entity;
    }
}
