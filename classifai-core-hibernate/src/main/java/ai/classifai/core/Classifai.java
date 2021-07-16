package ai.classifai.core;

import ai.classifai.core.entities.*;
import ai.classifai.core.entities.dto.*;
import ai.classifai.core.entities.dto.annotation.AnnotationDTO;
import ai.classifai.core.entities.dto.dataversion.DataVersionDTO;
import ai.classifai.core.services.repository.*;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class Classifai
{
    private final ProjectRepository projectRepository;
    private final VersionRepository versionRepository;
    private final LabelRepository labelRepository;
    private final DataRepository dataRepository;
    private final AnnotationRepository annotationRepository;
    private final DataVersionRepository dataVersionRepository;
    private final PointRepository pointRepository;

    //***************************************PROJECT**********************************************
    public Project createProject(@NonNull ProjectDTO dto)
    {
        return projectRepository.create(dto);
    }

    public List<? extends Project> listProjectByAnnotationType(@NonNull Integer annotationType)
    {
        return projectRepository.listByAnnotationType(annotationType);
    }

    public Project getProject(@NonNull UUID id)
    {
        return projectRepository.get(id);
    }

    /**
     * project must exist in order to call this method!
     * isPresent() is not called to get method, might introduce NullPointerException
     *
     * @param name project name
     * @param annotationType project annotation type
     * @return project entity
     */
    public Project findProject(@NonNull String name, @NonNull Integer annotationType)
    {
        return projectRepository.find(name, annotationType).get();
    }

    public void setStarProject(@NonNull Project project, @NonNull Boolean starred)
    {
        projectRepository.setStarred(project, starred);
    }

    public void renameProject(@NonNull Project project, @NonNull String name)
    {
        projectRepository.rename(project, name);
    }

    public void deleteProject(@NonNull Project project)
    {
        projectRepository.remove(project);
    }

    //***************************************VERSION**********************************************
    public Version createVersion(@NonNull VersionDTO dto)
    {
        return versionRepository.create(dto);
    }

    public void updateModifiedAt(@NonNull Version version)
    {
        versionRepository.updateModifiedAt(version);
    }

    //***************************************DATA**********************************************
    public Data createData(@NonNull DataDTO dto)
    {
        return dataRepository.create(dto);
    }

    public Data getData(@NonNull UUID id)
    {
        return dataRepository.get(id);
    }

    public void deleteData(@NonNull Data data)
    {
        dataRepository.remove(data);
    }

    //***************************************DATAVERSION**********************************************
    public DataVersion createDataVersion(@NonNull DataVersionDTO dto)
    {
        return dataVersionRepository.create(dto);
    }

    public DataVersion getDataVersion(@NonNull DataVersion.DataVersionId id)
    {
        return dataVersionRepository.get(id);
    }

    public DataVersion updateDataVersion(@NonNull DataVersionDTO dto)
    {
        return dataVersionRepository.update(dto);
    }

    public void deleteDataVersion(@NonNull DataVersion dataVersion)
    {
        dataVersionRepository.remove(dataVersion);
    }

    //***************************************ANNOTATION**********************************************
    public Annotation createAnnotation(@NonNull AnnotationDTO dto)
    {
        return annotationRepository.create(dto);
    }

    public Annotation getAnnotation(@NonNull Long id)
    {
        return annotationRepository.get(id);
    }

    public Annotation updateAnnotation(@NonNull AnnotationDTO dto)
    {
        return annotationRepository.update(dto);
    }

    public void deleteAnnotation(@NonNull Annotation annotation)
    {
        annotationRepository.remove(annotation);
    }

    //***************************************POINT**********************************************
    public Point createPoint(@NonNull PointDTO dto)
    {
        return pointRepository.create(dto);
    }

    public Point getPoint(@NonNull UUID id)
    {
        return pointRepository.get(id);
    }

    public Point updatePoint(@NonNull PointDTO dto)
    {
        return pointRepository.update(dto);
    }

    public void deletePoint(@NonNull Point point)
    {
        pointRepository.remove(point);
    }

    //***************************************LABEL**********************************************
    public Label createLabel(@NonNull LabelDTO dto)
    {
        return labelRepository.create(dto);
    }

    public Label getLabel(@NonNull UUID id)
    {
        return labelRepository.get(id);
    }

    public Label updateLabel(@NonNull LabelDTO dto)
    {
        return labelRepository.update(dto);
    }

    public void deleteLabel(@NonNull Label label)
    {
        labelRepository.remove(label);
    }
}
