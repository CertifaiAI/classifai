package ai.classifai.core.entity.dto.generic;

import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class DataVersionDTO
{
    UUID dataId;
    UUID versionId;

    @Builder.Default
    List<Long> annotationIdList = new ArrayList<>();

}
