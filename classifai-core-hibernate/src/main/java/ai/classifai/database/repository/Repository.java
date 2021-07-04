package ai.classifai.database.repository;

import ai.classifai.database.model.Model;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.Arrays;

@Slf4j
public abstract class Repository
{
    protected EntityManager entityManager;

    public Repository(EntityManager entityManager)
    {
        this.entityManager = entityManager;
    }

    protected void persist(Runnable function)
    {
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();

        function.run();

        transaction.commit();
    }

    protected void saveItem(Object... obj)
    {
        persist(() -> Arrays.stream(obj)
                .forEach(this::persistObject));
    }

    protected void removeItem(Object... obj)
    {
        persist(() -> Arrays.stream(obj)
                .forEach(o -> entityManager.remove(getObjectFromEntityManager(o))));
    }

    private Object getObjectFromEntityManager(Object obj)
    {
        return entityManager.contains(obj) ? obj : entityManager.merge(obj);
    }

    private void persistObject(Object obj)
    {
        Model model = (Model) obj;

        if (model.isPersisted())
        {
            entityManager.merge(model);
        }
        else
        {
            entityManager.persist(model);
        }
    }

    public void closeEntityManager()
    {
        entityManager.close();
    }

    public static void closeRepositories(Repository... repositories)
    {
        Arrays.stream(repositories).forEach(Repository::closeEntityManager);
    }
}
