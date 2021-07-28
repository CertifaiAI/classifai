package ai.classifai.core.service.generic;

import ai.classifai.core.entity.dto.generic.DataVersionDTO;
import ai.classifai.core.entity.model.generic.DataVersion;
import ai.classifai.core.entity.trait.HasId;
import lombok.NonNull;

import java.util.List;
import java.util.UUID;

public interface Repository<Entity extends HasId<Id>, DTO, Id> extends AbstractRepository<Entity, DTO, Id>
{
    Entity create(@NonNull DTO dto);
}
