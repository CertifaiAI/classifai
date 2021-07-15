package ai.classifai.db.repository;

import ai.classifai.db.entities.VersionEntity;
import ai.classifai.db.entities.annotation.AnnotationEntity;
import ai.classifai.db.entities.data.DataEntity;
import ai.classifai.db.handler.DataVersionHandler;
import ai.classifai.db.handler.LabelHandler;
import ai.classifai.db.entities.LabelEntity;
import ai.classifai.db.entities.ProjectEntity;
import ai.classifai.db.entities.dataVersion.DataVersionEntity;

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

    public ProjectEntity getProjectByNameAndAnnotation(String projectName, Integer annotationTypeIndex)
    {
        TypedQuery<ProjectEntity> query = entityManager.createQuery("select p from ProjectEntity p where p.annotationType = " +
                ":annotationType and p.projectName = :projectName", ProjectEntity.class);
        query.setParameter("annotationType", annotationTypeIndex);
        query.setParameter("projectName", projectName);

        ProjectEntity result = null;

        try
        {
            result = query.getSingleResult();
        }
        catch (Exception ignored) {}

        return result;
    }

    @SuppressWarnings("unchecked")
    public List<ProjectEntity> getProjectListByAnnotation(Integer annotationTypeIndex)
    {
        Query query = entityManager.createQuery("select p from ProjectEntity p where p.annotationType = :annotationType");
        query.setParameter("annotationType", annotationTypeIndex);

        return (List<ProjectEntity>) query.getResultList();
    }

    public void saveProject(ProjectEntity project)
    {
        List<VersionEntity> versionEntityList = project.getVersionEntityList();
        List<LabelEntity> labelEntityList = LabelHandler.getLabelListFromVersionList(versionEntityList);
        List<DataEntity> dataEntityList = project.getDataList();
        List<DataVersionEntity> dataVersionEntityList = DataVersionHandler.getDataVersionListFromDataList(dataEntityList);

        saveItem(project);
        saveItem(labelEntityList.toArray());
        saveItem(dataVersionEntityList.toArray());
    }

    public void deleteProject(ProjectEntity project) {
        List<VersionEntity> versionEntityList = project.getVersionEntityList();
        List<LabelEntity> labelEntityList = LabelHandler.getLabelListFromVersionList(versionEntityList);
        List<DataEntity> dataEntityList = project.getDataList();
        List<DataVersionEntity> dataVersionEntityList = DataVersionHandler.getDataVersionListFromDataList(dataEntityList);
        List<AnnotationEntity> annotationEntityList = DataVersionEntity.getAnnotationListFromDataVersionList(dataVersionEntityList);

        // remove annotation
        removeItem((Object[]) annotationEntityList.toArray(AnnotationEntity[]::new));

        // remove dataversion
        removeItem((Object[]) dataVersionEntityList.toArray(DataVersionEntity[]::new));

        // remove data
        removeItem((Object[]) dataEntityList.toArray(DataEntity[]::new));

        // remove label
        removeItem((Object[]) labelEntityList.toArray(LabelEntity[]::new));

        // remove version
        removeItem((Object[]) versionEntityList.toArray(VersionEntity[]::new));

        // remove project
        removeItem(project);
    }
}
