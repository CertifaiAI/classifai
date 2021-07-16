package ai.classifai.core.services.repository;

import ai.classifai.core.entities.traits.HasId;
import lombok.NonNull;

import java.util.List;

public interface Repository<T extends HasId<R>, S, R>
{
    T create(@NonNull S dto);

    T get(@NonNull R id);

    T update(@NonNull S dto);

    void remove(@NonNull T entity);

    List<? extends T> list();
}
