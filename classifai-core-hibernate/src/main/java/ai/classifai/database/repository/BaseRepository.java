package ai.classifai.database.repository;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public abstract class BaseRepository
{
    protected EntityManager entityManager;

    public BaseRepository(EntityManager entityManager)
    {
        this.entityManager = entityManager;
    }

    protected void saveItem(Runnable function)
    {
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();

        function.run();

        transaction.commit();
    }

    public void closeEntityManager()
    {
        entityManager.close();
    }
}
