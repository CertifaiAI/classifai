package ai.classifai.db.repository;

import ai.classifai.core.entities.traits.HasId;
import ai.classifai.core.services.repository.Repository;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.function.Function;

@AllArgsConstructor
public abstract class JPARepository<T extends HasId<R>, S, R> implements Repository<T, S, R>
{
    protected EntityManager em;
    protected Function<S, T> fromDTO;

    @Override
    public T create(@NonNull S dto)
    {
        T entity = fromDTO.apply(dto);

        commit(() -> em.persist(entity));

        return entity;
    }

    @Override
    public T update(@NonNull S dto)
    {
        T entity = fromDTO.apply(dto);

        commit(() -> em.merge(entity));

        return entity;
    }

    @Override
    public void remove(@NonNull T entity)
    {
        commit(() -> em.remove(entity));
    }

    protected void commit(Runnable function)
    {
        EntityTransaction transaction = em.getTransaction();

        transaction.begin();

        function.run();

        transaction.commit();
    }


}
