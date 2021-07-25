package ai.classifai.core.service.generic;

import ai.classifai.core.entity.dto.generic.DataDTO;
import ai.classifai.core.entity.model.generic.Project;
import ai.classifai.core.entity.dto.generic.ProjectDTO;
import ai.classifai.core.entity.model.generic.Version;
import ai.classifai.core.entity.model.generic.Data;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends Repository<Project, ProjectDTO, UUID>
{
    Optional<Project> findByNameAndType(@NonNull String name, @NonNull Integer type);

    List<Project> listByType(@NonNull Integer type);

    Project rename(Project project, @NonNull String newName);

    Project star(Project project, @NonNull Boolean isStarred);

    Project setNew(Project project, @NonNull Boolean isNew);

    Project setCurrentVersion(Project project, @NonNull Version version);

    Project addVersion(Project project, @NonNull Version version);

    Project addData(Project project, @NonNull Data data);

    Project addDataList(Project project, @NonNull List<? extends Data> dataList);
}
