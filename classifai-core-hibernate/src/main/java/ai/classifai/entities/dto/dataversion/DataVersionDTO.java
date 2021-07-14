package ai.classifai.entities.dto.dataversion;

import ai.classifai.entities.dto.ExportDTO;
import lombok.*;

import java.io.Serial;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class DataVersionDTO extends ExportDTO<DataVersionDTO>
{
    @Serial
    private static final long serialVersionUID = -866720383224702998L;

    UUID dataId;
    UUID versionId;

    List<Long> annotationIds;
}
