package ai.classifai.entities;

import ai.classifai.entities.dto.VersionDTO;
import ai.classifai.entities.traits.HasDTO;
import ai.classifai.entities.traits.HasId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface Version extends HasId<UUID>, HasDTO<VersionDTO>
{
    LocalDateTime getCreatedDate();
    LocalDateTime getLastModifiedDate();

    List<Label> getLabelList();
}
