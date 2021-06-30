package ai.classifai.database.repository;

import ai.classifai.database.model.Project;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;

public class ProjectRepository 
{
    private EntityManager entityManager;

    public ProjectRepository(EntityManager entityManager)
    {
        this.entityManager = entityManager;
    }

    public Project getProjectByNameAndAnnotation(String projectName, Integer annotationTypeIndex)
    {
        TypedQuery<Project> query = entityManager.createQuery("select p from Project p where p.annoType = :annoType and p.projectName = :projectName", Project.class);
        query.setParameter("annoType", annotationTypeIndex);
        query.setParameter("projectName", projectName);

        try
        {
            return query.getSingleResult();
        }
        catch (Exception ignored)
        {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<Project> getProjectListByAnnotation(Integer annotationTypeIndex)
    {
        Query query = entityManager.createQuery("select p from Project p where p.annoType = :annoType");
        query.setParameter("annoType", annotationTypeIndex);

        return query.getResultList();
    }

    public Project createNewProject(Project project)
    {
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();

        if (project.getProjectId() == null)
        {
            entityManager.persist(project);
        }
        else
        {
            entityManager.merge(project);
        }

        transaction.commit();

        return project;
    }
}
