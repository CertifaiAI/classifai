package ai.classifai.database.repository;

import ai.classifai.database.model.Project;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;

public class ProjectRepository extends BaseRepository
{
    public ProjectRepository(EntityManager entityManager)
    {
        super(entityManager);
    }

    public Project getProjectByNameAndAnnotation(String projectName, Integer annotationTypeIndex)
    {
        TypedQuery<Project> query = entityManager.createQuery("select p from Project p where p.annoType = :annoType and p.projectName = :projectName", Project.class);
        query.setParameter("annoType", annotationTypeIndex);
        query.setParameter("projectName", projectName);

        Project result = null;

        try
        {
            result = query.getSingleResult();
        }
        catch (Exception ignored) {}

        return result;
    }

    @SuppressWarnings("unchecked")
    public List<Project> getProjectListByAnnotation(Integer annotationTypeIndex)
    {
        Query query = entityManager.createQuery("select p from Project p where p.annoType = :annoType");
        query.setParameter("annoType", annotationTypeIndex);

        List<Project> result = query.getResultList();

        return result;
    }

    public void saveProject(Project project)
    {
        saveItem(() -> this.addProject(project));
    }

    private void addProject(Project project)
    {
        if (project.getProjectId() == null)
        {
            entityManager.persist(project);
        }
        else
        {
            entityManager.merge(project);
        }
    }
}
