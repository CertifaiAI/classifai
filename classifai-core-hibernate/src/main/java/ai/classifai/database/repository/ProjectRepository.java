package ai.classifai.database.repository;

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

        return (List<Project>) query.getResultList();
    }

    public void saveProject(Project project)
    {
        List<Version> versionList = project.getVersionList();
        List<Label> labelList = Label.getLabelListFromVersionList(versionList);
        List<Data> dataList = project.getDataList();
        List<DataVersion> dataVersionList = Data.getDataVersionListFromDataList(dataList);
        List<Annotation> annotationList = DataVersion.getAnnotationListFromDataVersionList(dataVersionList);

        // save project
        saveItem(project);

        // save version
        saveItem((Object[]) versionList.toArray(Version[]::new));

        // save label
        saveItem((Object[]) labelList.toArray(Label[]::new));

        // save data
        saveItem((Object[]) dataList.toArray(Data[]::new));

        // save dataversion
        saveItem((Object[]) dataVersionList.toArray(DataVersion[]::new));

        // save annotation
        saveItem((Object[]) annotationList.toArray(Annotation[]::new));
    }

    public void deleteProject(Project project) {
        List<Version> versionList = project.getVersionList();
        List<Label> labelList = Label.getLabelListFromVersionList(versionList);
        List<Data> dataList = project.getDataList();
        List<DataVersion> dataVersionList = Data.getDataVersionListFromDataList(dataList);
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
