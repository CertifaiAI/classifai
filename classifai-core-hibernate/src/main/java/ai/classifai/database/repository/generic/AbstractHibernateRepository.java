package ai.classifai.database.repository.generic;

import ai.classifai.core.entity.trait.HasId;
import ai.classifai.core.service.generic.Repository;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public abstract class AbstractHibernateRepository<Entity extends HasId<Id>, DTO, Id, EntityImpl extends Entity> implements Repository<Entity, DTO, Id>
{
    protected EntityManager em;
    protected final Class<? extends EntityImpl> entityClass;

    public static final String CLASS_CAST_ERROR_MESSAGE = "Entity given is not hibernate entity, wrong pair of repository and entity is used!";

    @Override
    public void delete(@NonNull Entity entity)
    {
        if (! em.contains(entity))
        {
            entity = em.merge(entity);
        }

        em.remove(entity);
    }

    @Override
    public Entity get(@NonNull Id id)
    {
        return em.find(entityClass, id);
    }

    @SuppressWarnings("unchecked")
    protected EntityImpl toEntityImpl(Entity entity)
    {
        try
        {
            return (EntityImpl) entity;
        }
        catch (ClassCastException e)
        {
            throw new IllegalArgumentException(CLASS_CAST_ERROR_MESSAGE);
        }
    }

    @Override
    public void deleteList(@NonNull List<Entity> entityList)
    {
        entityList.forEach(this::delete);
    }
}
