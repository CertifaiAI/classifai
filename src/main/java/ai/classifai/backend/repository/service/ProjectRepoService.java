package ai.classifai.backend.repository.service;

import ai.classifai.backend.data.enumeration.ProjectType;
import ai.classifai.backend.dto.ProjectDTO;
import ai.classifai.backend.repository.SqlQueries;
import ai.classifai.backend.repository.entity.project.ProjectEntity;
import ai.classifai.backend.utility.StringUtility;
import ai.classifai.backend.repository.JdbcHolder;
import ai.classifai.core.services.project.ProjectRepository;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
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
    private final JDBCPool annotationPool;

    public ProjectRepoService(JdbcHolder jdbcHolder) {
        this.projectPool = jdbcHolder.getProjectPool();
        this.annotationPool = jdbcHolder.getAnnotationPool();
    }

    private Future<RowSet<Row>> runQuery(String query, Tuple params, JDBCPool pool) {
        final Promise<RowSet<Row>> promise = Promise.promise();
        pool.preparedQuery(query)
                .execute(params)
                .onComplete(fetch -> {
                    if(fetch.succeeded()) {
                        promise.complete(fetch.result());
                    } else {
                        promise.fail(fetch.cause());
                    }
                });
        return promise.future();
    }

    private Future<RowSet<Row>> runQuery(String query, JDBCPool pool) {
        final Promise<RowSet<Row>> promise = Promise.promise();
        pool.preparedQuery(query)
                .execute()
                .onComplete(fetch -> {
                    if(fetch.succeeded()) {
                        promise.complete(fetch.result());
                    } else {
                        promise.fail(fetch.cause());
                    }
                });
        return promise.future();
    }

    @Override
    public ProjectEntity toProjectEntity(@NonNull ProjectDTO projectDTO) {
        return ProjectEntity.builder()
                .projectId(projectDTO.getProjectId())
                .projectName(projectDTO.getProjectName())
                .projectType(projectDTO.getProjectType())
                .projectPath(projectDTO.getProjectPath())
                .projectInfra(projectDTO.getProjectInfra())
                .labelList(projectDTO.getLabelList())
                .build();
    }

    private ProjectEntity toProjectEntity(@NonNull Row row) {
        return ProjectEntity.builder()
                    .projectId(row.getString("PROJECT_ID"))
                    .projectName(row.getString("PROJECT_NAME"))
                    .projectType(row.getInteger("ANNOTATION_TYPE"))
                    .labelList(StringUtility.convertStringListToListString(row.getString("LABEL_LIST")))
                    .build();
    }

    private ProjectEntity createProjectEntityFromDTO(@NonNull ProjectDTO projectDTO) {
        return ProjectEntity.builder()
                .projectName(projectDTO.getProjectName())
                .projectType(projectDTO.getProjectType())
                .labelList(projectDTO.getLabelList())
                .build();
    }

    @Override
    public Future<ProjectEntity> createProject(@NonNull ProjectDTO projectDTO) {
        ProjectEntity projectEntity = toProjectEntity(projectDTO);
        Tuple params = Tuple.of(projectEntity.getProjectId(), projectEntity.getProjectName(), projectEntity.getProjectType(),
                projectEntity.getProjectPath(), projectEntity.getProjectInfra(), String.valueOf(projectEntity.getLabelList()));

        return runQuery(SqlQueries.getCreateProject(), params, projectPool)
                .map(response -> {
                    log.info("Project " + projectDTO.getProjectName() + " created");
                    return ProjectEntity.builder()
                            .projectName(projectDTO.getProjectName())
                            .projectType(projectDTO.getProjectType())
                            .labelList(projectDTO.getLabelList())
                            .build();
                });
    }

    @Override
    public Future<List<ProjectEntity>> listProjects(Integer projectType) {
        return runQuery(SqlQueries.getListAllProject(), projectPool)
                .map(response -> {
                    if (response.size() != 0) {
                        List<ProjectEntity> projectEntityList = new ArrayList<>();
                        for (Row row : response) {
                            projectEntityList.add(toProjectEntity(row));
                        }
                        log.info(projectEntityList.toString());
                        log.info("Retrieve projects for " + ProjectType.getProjectTypeName(projectType));
                        return projectEntityList.stream()
                                .filter(res -> res.getProjectType() == projectType)
                                .collect(Collectors.toList());
                    }
                    log.info("Fail to retrieve all project for project type: " + ProjectType.getProjectTypeName(projectType));
                    return null;
                });
    }

    @Override
    public Future<Optional<ProjectEntity>> getProjectById(@NonNull String projectId) {
        Tuple params = Tuple.of(projectId);
        return runQuery(SqlQueries.getRetrieveProjectById(), params, projectPool)
                .map(res -> {
                    if (res.size() != 0) {
                        return Optional.ofNullable(toProjectEntity(res.iterator().next()));
                    }
                    return Optional.empty();
                });
    }

    @Override
    public Future<ProjectEntity> updateProject(@NonNull ProjectEntity projectEntity, @NonNull ProjectDTO projectDTO) {
        Tuple params = Tuple.of(projectEntity.getProjectId(), projectEntity.getProjectName(), projectEntity.getProjectType(),
                projectEntity.getProjectPath(), projectEntity.getProjectInfra(), projectEntity.getLabelList());

        return runQuery(SqlQueries.getUpdateProject(), params, projectPool)
                .map(res -> createProjectEntityFromDTO(projectDTO));
    }

    @Override
    public Future<Void> deleteProjectById(@NonNull ProjectEntity projectEntity) {
        Tuple params = Tuple.of(projectEntity.getProjectId());
        return runQuery(SqlQueries.getDeleteProjectById(), params, projectPool)
                .map(res -> {
                    log.info("Delete project " + projectEntity.getProjectName());
                    return null;
                });
    }

}
