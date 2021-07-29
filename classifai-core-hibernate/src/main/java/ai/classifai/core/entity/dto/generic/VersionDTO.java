package ai.classifai.core.entity.dto.generic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO class representing Version entity
 *
 * @author YinChuangSum
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VersionDTO {
    @Builder.Default
    private UUID id = null;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime modifiedAt = LocalDateTime.now();

    @Builder.Default
    private List<UUID> labelIdList = new ArrayList<>();

    @Builder.Default
    private UUID projectId = null;
}
