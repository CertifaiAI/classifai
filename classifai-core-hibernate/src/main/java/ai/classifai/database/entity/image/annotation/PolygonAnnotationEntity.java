package ai.classifai.database.entity.image.annotation;

import ai.classifai.core.entity.dto.generic.AnnotationDTO;
import ai.classifai.core.entity.dto.image.annotation.PolygonAnnotationDTO;
import ai.classifai.core.entity.model.image.annotation.PolygonAnnotation;
import ai.classifai.core.entity.trait.HasId;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Getter
@Entity(name = "polygon_annotation")
public class PolygonAnnotationEntity extends ImageAnnotationEntity implements PolygonAnnotation
{
    public PolygonAnnotationEntity()
    {
        super();
    }

    @Override
    public PolygonAnnotationDTO toDTO()
    {
        return PolygonAnnotationDTO.builder()
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
        PolygonAnnotationDTO dto = PolygonAnnotationDTO.toDTOImpl(annotationDTO);
        setId(dto.getId());
        setPosition(dto.getPosition());
        update(dto);
    }

    @Override
    public void update(AnnotationDTO annotationDTO) {}
}
