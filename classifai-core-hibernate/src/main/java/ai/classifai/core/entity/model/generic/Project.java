package ai.classifai.core.entity.model.generic;

import ai.classifai.core.entity.dto.generic.ProjectDTO;
import ai.classifai.core.entity.dto.generic.DataDTO;
import ai.classifai.core.entity.trait.HasDTO;
import ai.classifai.core.entity.trait.HasId;

import java.util.List;
import java.util.UUID;

/**
 * Project entity interface
 *
 * @author YinChuangSum
 */
public interface Project extends HasDTO<ProjectDTO>, HasId<UUID>
{
    String getName();

    Integer getType();

    String getPath();

    Boolean isNew();

    Boolean isStarred();

    Integer getInfra();

    Version getCurrentVersion();

    List<Version> getVersionList();

    List<Data> getDataList();
}
