package ai.classifai.database.entity.image.annotation;

import ai.classifai.core.entity.dto.generic.AnnotationDTO;
import ai.classifai.core.entity.dto.image.annotation.BoundingBoxAnnotationDTO;
import ai.classifai.core.entity.model.image.annotation.BoundingBoxAnnotation;
import ai.classifai.core.entity.trait.HasId;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.stream.Collectors;

@Getter
@Entity(name = "boundingbox_annotation")
public class BoundingBoxAnnotationEntity extends ImageAnnotationEntity implements BoundingBoxAnnotation
{
    public BoundingBoxAnnotationEntity()
    {
        super();
    }

    @Override
    public BoundingBoxAnnotationDTO toDTO()
    {
        return BoundingBoxAnnotationDTO.builder()
                .id(getId())
                .position(getPosition())
                .labelId(getLabel().getId())
                .dataId(getDataVersion().getId().getDataId())
                .versionId(getDataVersion().getId().getVersionId())
                .pointIdList(getPointList().stream()
                        .map(HasId::getId)
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public void fromDTO(AnnotationDTO annotationDTO)
    {
        BoundingBoxAnnotationDTO dto = BoundingBoxAnnotationDTO.toDTOImpl(annotationDTO);
        setId(dto.getId());
        update(dto);
    }

    @Override
    public void update(AnnotationDTO annotationDTO)
    {
        BoundingBoxAnnotationDTO dto = BoundingBoxAnnotationDTO.toDTOImpl(annotationDTO);
        setPosition(dto.getPosition());
    }
}
