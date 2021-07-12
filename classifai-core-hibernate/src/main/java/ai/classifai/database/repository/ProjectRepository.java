package ai.classifai.database.repository;

import ai.classifai.database.handler.DataVersionHandler;
import ai.classifai.database.handler.LabelHandler;
import ai.classifai.database.model.Label;
import ai.classifai.database.model.Project;
import ai.classifai.database.model.Version;
import ai.classifai.database.model.annotation.Annotation;
import ai.classifai.database.model.data.Data;
import ai.classifai.database.model.dataVersion.DataVersion;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;

public class ProjectRepository extends Repository
{
    public ProjectRepository(EntityManager entityManager)
    {
        super(entityManager);
    }

    public Project getProjectByNameAndAnnotation(String projectName, Integer annotationTypeIndex)
    {
        TypedQuery<Project> query = entityManager.createQuery("select p from Project p where p.annotationType = " +
                ":annotationType and p.projectName = :projectName", Project.class);
        query.setParameter("annotationType", annotationTypeIndex);
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
        Query query = entityManager.createQuery("select p from Project p where p.annotationType = :annotationType");
        query.setParameter("annotationType", annotationTypeIndex);

        return (List<Project>) query.getResultList();
    }

    public void saveProject(Project project)
    {
        List<Version> versionList = project.getVersionList();
        List<Label> labelList = LabelHandler.getLabelListFromVersionList(versionList);
        List<Data> dataList = project.getDataList();
        List<DataVersion> dataVersionList = DataVersionHandler.getDataVersionListFromDataList(dataList);

        saveItem(project);
        saveItem(labelList.toArray());
        saveItem(dataVersionList.toArray());
    }

    public void deleteProject(Project project) {
        List<Version> versionList = project.getVersionList();
        List<Label> labelList = LabelHandler.getLabelListFromVersionList(versionList);
        List<Data> dataList = project.getDataList();
        List<DataVersion> dataVersionList = DataVersionHandler.getDataVersionListFromDataList(dataList);
        List<Annotation> annotationList = DataVersion.getAnnotationListFromDataVersionList(dataVersionList);

        // remove annotation
        removeItem((Object[]) annotationList.toArray(Annotation[]::new));

        // remove dataversion
        removeItem((Object[]) dataVersionList.toArray(DataVersion[]::new));

        // remove data
        removeItem((Object[]) dataList.toArray(Data[]::new));

        // remove label
        removeItem((Object[]) labelList.toArray(Label[]::new));

        // remove version
        removeItem((Object[]) versionList.toArray(Version[]::new));

        // remove project
        removeItem(project);
    }
}
