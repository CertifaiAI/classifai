package ai.classifai.entities.dto.annotation.image;

import ai.classifai.entities.dto.annotation.AnnotationDTO;
import lombok.*;

import java.io.Serial;

@lombok.Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class ImageAnnotationDTO extends AnnotationDTO
{
    @Serial
    private static final long serialVersionUID = 780032067162471315L;

    public ImageAnnotationDTO(long annotationId, String dataId, String versionId, int position)
    {
        super(annotationId, dataId, versionId, position);
    }

}
