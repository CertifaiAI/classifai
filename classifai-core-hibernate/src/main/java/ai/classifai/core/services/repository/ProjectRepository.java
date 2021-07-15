package ai.classifai.core.services.repository;

import ai.classifai.core.entities.Project;
import ai.classifai.core.entities.dto.ProjectDTO;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends Repository<Project, ProjectDTO, UUID>
{
    List<Project> listByAnnotationType(@NonNull Integer annotationType);

    Optional<Project> find(@NonNull String name, @NonNull Integer annotationType);

    Project rename(Project project, @NonNull String newName);

    Project star(Project project);
    Project unstar(Project project);

    Project reload(Project project);
}
