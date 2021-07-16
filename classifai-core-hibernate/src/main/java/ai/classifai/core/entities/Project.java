package ai.classifai.core.entities;

import ai.classifai.core.entities.traits.HasDTO;
import ai.classifai.core.entities.traits.HasId;
import ai.classifai.core.entities.dto.ProjectDTO;

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

    void setName(String name);
    void setAnnotationType(Integer annotationType);
    void setPath(String path);
    void setIsStarred(Boolean starred);
    void setInfra(Integer infra);
    void setCurrentVersion(Version currentVersion);
}
