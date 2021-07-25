package ai.classifai.core.entity.model.generic;

import ai.classifai.core.entity.dto.generic.VersionDTO;
import ai.classifai.core.entity.trait.HasDTO;
import ai.classifai.core.entity.trait.HasId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface Version extends HasDTO<VersionDTO>, HasId<UUID>
{
    LocalDateTime getCreatedAt();

    LocalDateTime getModifiedAt();

    List<Label> getLabelList();
}
