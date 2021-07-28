package ai.classifai.core.service.generic;

import ai.classifai.core.entity.trait.HasId;
import lombok.NonNull;

import java.util.List;

public interface AbstractRepository <Entity extends HasId<Id>, DTO, Id>
{
    Entity get(@NonNull Id id);

//    Entity update(@NonNull Entity entity, @NonNull DTO dto);

    void delete(@NonNull Entity entity);
}
