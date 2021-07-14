package ai.classifai.entities.dto.annotation;

import ai.classifai.entities.dto.ExportDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class AnnotationDTO extends ExportDTO<AnnotationDTO>
{
    @Serial
    private static final long serialVersionUID = 3228386957944320822L;

    Long id;
    UUID labelId;
    Integer position;
}
