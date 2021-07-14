package ai.classifai.entities;

import ai.classifai.entities.dto.ProjectDTO;
import ai.classifai.entities.traits.HasDTO;
import ai.classifai.entities.traits.HasId;

import java.util.List;
import java.util.UUID;

public interface Project extends HasId<UUID>, HasDTO<ProjectDTO>
{
    String getName();
    Integer getAnnotationType();
    String getPath();
    Boolean isStarred();
    Integer getInfra();
    Version getCurrentVersion();

    List<Version> getVersionList();
    List<Data> getDataList();
}
