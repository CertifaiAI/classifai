package ai.classifai.core.service.generic;

import ai.classifai.core.entity.trait.HasId;
import lombok.NonNull;

import java.util.List;

/**
 * Abstract repository interface with methods read, update and delete. Create method is not included.
 * Implemented by repository of abstract entity such as Data, DataVersion, Annotation and etc.
 *
 * @author YinChuangSum
 */
public interface AbstractRepository <Entity extends HasId<Id>, DTO, Id>
{
    Entity get(@NonNull Id id);

//    Entity update(@NonNull Entity entity, @NonNull DTO dto);

    void delete(@NonNull Entity entity);
}
