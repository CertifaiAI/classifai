package ai.classifai.core.entity.dto.generic;

import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class DataDTO
{
    @Builder.Default
    UUID id = null;

    String path;
    String checksum;
    Long fileSize;

    @Builder.Default
    UUID projectId = null;
}
