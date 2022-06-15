package ai.classifai.backend.repository.service;

import ai.classifai.backend.repository.database.DBUtils;
import ai.classifai.backend.repository.JdbcHolder;
import ai.classifai.backend.repository.query.QueryOps;
import ai.classifai.backend.repository.SqlQueries;
import ai.classifai.backend.utility.JsonUtility;
import ai.classifai.core.dto.ProjectDTO;
import ai.classifai.core.entity.project.Project;
import ai.classifai.core.enumeration.AnnotationType;
import ai.classifai.core.service.project.ProjectRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.vertx.core.Future;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class ProjectRepoService implements ProjectRepository {
    private final JDBCPool projectPool;
    private final QueryOps queryOps = new QueryOps();

    public ProjectRepoService(JdbcHolder jdbcHolder) {
        this.projectPool = jdbcHolder.getProjectPool();
    }

    @Override
    public Future<Project> createProject(@NonNull ProjectDTO projectDTO) {
        Tuple params = getTuple(projectDTO);
        return queryOps.runQuery(SqlQueries.getCreateProject(), params, projectPool)
                .map(response -> {
                    log.info("Project " + projectDTO.getProjectName() + " created");
                    return toProjectEntity(projectDTO);
                });
    }

    @Override
    public Future<List<Project>> listProjects(@NonNull Integer annotationType) {
        return queryOps.runQuery(SqlQueries.getListAllProject(), projectPool)
                .map(response -> {
                    if (response.size() != 0) {
                        List<Project> projectEntityList = new ArrayList<>();
                        for (Row row : response) {
                            try {
                                projectEntityList.add(toProjectEntity(row));
                            } catch (JsonProcessingException exception) {
                                exception.printStackTrace();
                            }
                        }
                        return projectEntityList.stream()
                                .filter(res -> res.getAnnotationType().equals(annotationType))
                                .collect(Collectors.toList());
                    }
                    log.info("Fail to retrieve all project for project type: " + AnnotationType.get(annotationType).name());
                    return null;
                });
    }

    @Override
    public Future<Optional<Project>> getProjectById(@NonNull String projectId) {
        Tuple params = Tuple.of(projectId);
        return queryOps.runQuery(SqlQueries.getRetrieveProjectById(), params, projectPool)
                .map(res -> {
                    Optional<Project> project = Optional.empty();
                    if (res.size() != 0) {
                        try {
                            project = Optional.ofNullable(toProjectEntity(res.iterator().next()));
                        } catch (JsonProcessingException exception) {
                            exception.printStackTrace();
                        }
                        return project;
                    }
                    return project;
                });
    }

    @Override
    public Future<Project> updateProject(@NonNull Project projectEntity, @NonNull ProjectDTO projectDTO) {
        Tuple params = getTuple(projectDTO);
        return queryOps.runQuery(SqlQueries.getUpdateProject(), params, projectPool)
                .map(res -> projectEntity);
    }

    @Override
    public Future<Void> deleteProjectById(@NonNull Project projectEntity) {
        Tuple params = Tuple.of(projectEntity.getProjectId());
        return queryOps.runQuery(SqlQueries.getDeleteProjectById(), params, projectPool)
                .map(res -> {
                    log.info("Delete project " + projectEntity.getProjectName());
                    return DBUtils.toVoid(null);
                })
                .onFailure(res -> log.info(res.getCause().getMessage()));
    }

    private Project toProjectEntity(@NonNull ProjectDTO projectDTO) {
        return Project.builder()
                .projectId(projectDTO.getProjectId())
                .projectName(projectDTO.getProjectName())
                .projectPath(projectDTO.getProjectPath())
                .annotationType(projectDTO.getAnnotationType())
                .projectInfra(projectDTO.getProjectInfra())
                .labelList(projectDTO.getLabelList())
                .build();
    }

    private Project toProjectEntity(Row row) throws JsonProcessingException {
        return Project.builder()
                .projectId(row.getString("project_id"))
                .projectName(row.getString("project_name"))
                .projectPath(row.getString("project_path"))
                .annotationType(row.getInteger("annotation_type"))
                .projectInfra(row.getInteger("project_infra"))
                .labelList(JsonUtility.parseJsonToList(row.getString("label_list")))
                .build();
    }

    private Tuple getTuple(ProjectDTO projectDTO) {
        return Tuple.of(
                projectDTO.getProjectId(),
                projectDTO.getProjectName(),
                projectDTO.getAnnotationType(),
                projectDTO.getProjectPath(),
                projectDTO.getProjectInfra(),
                projectDTO.getLabelList()
        );
    }

}
