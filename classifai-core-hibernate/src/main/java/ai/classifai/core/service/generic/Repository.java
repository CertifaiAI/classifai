package ai.classifai.core.service.generic;

import ai.classifai.core.entity.trait.HasId;
import lombok.NonNull;

import java.util.List;
import java.util.UUID;

public interface Repository<Entity extends HasId<Id>, DTO, Id>
{
    Entity create(@NonNull DTO dto);

    Entity get(@NonNull Id id);

//    Entity update(@NonNull Entity entity, @NonNull DTO dto);

    void delete(@NonNull Entity entity);

    void deleteList(@NonNull List<Entity> entityList);
}
