package ai.classifai.repository.service;

import ai.classifai.dto.ProjectDTO;
import ai.classifai.repository.JdbcHolder;
import ai.classifai.repository.SqlQueries;
import ai.classifai.repository.project.ProjectEntity;
import ai.classifai.utility.StringUtility;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ProjectRepoService implements ProjectRepository {
    JDBCPool projectPool = JdbcHolder.getProjectPool();
    JDBCPool annotationPool = JdbcHolder.getAnnotationPool();

    @Override
    public ProjectDTO toProjectDTO(@NonNull ProjectEntity projectEntity) {
        return ProjectDTO.builder()
                .projectName(projectEntity.getProjectName())
                .projectType(projectEntity.getProjectType())
                .labelList(projectEntity.getLabelList())
                .build();
    }

    @Override
    public Future<ProjectDTO> createProject(@NonNull ProjectEntity projectEntity) {
        Promise<ProjectDTO> promise = Promise.promise();
        Tuple params = Tuple.of(projectEntity.getProjectId(), projectEntity.getProjectName(), projectEntity.getProjectType(),
                projectEntity.getProjectPath(), projectEntity.getProjectInfra(), projectEntity.getLabelList());

        projectPool.preparedQuery(SqlQueries.getCreateProject())
                .execute(params)
                .onComplete(res -> {
                    if (res.succeeded()) {
                        promise.complete(toProjectDTO(projectEntity));
                        log.info("Project " + projectEntity.getProjectName() + " created");
                    }

                    else if (res.failed()) {
                        promise.fail(res.cause());
                    }
                });

        return promise.future();
    }

    @Override
    public List<ProjectDTO> listProjects() {
        List<ProjectDTO> projectDTOList = new ArrayList<>();

        projectPool.preparedQuery(SqlQueries.getListAllProject())
                .execute()
                .onComplete(res -> {
                    if (res.succeeded()) {
                        for(Row row : res.result().value()) {
                            ProjectEntity projectEntity = ProjectEntity.builder()
                                    .projectId(row.getString("project_id"))
                                    .projectName(row.getString("project_name"))
                                    .projectPath(row.getString("project_path"))
                                    .projectType(row.getInteger("annotation_type"))
                                    .projectInfra(row.getInteger("project_infra"))
                                    .labelList(StringUtility.convertStringListToListString(row.getString("label_list")))
                                    .build();

                            projectDTOList.add(toProjectDTO(projectEntity));
                        }
                    }

                    else if (res.failed()) {
                        log.info(res.cause().getMessage());
                    }
                });
        return projectDTOList;
    }

    @Override
    public ProjectDTO getProjectById(@NonNull ProjectEntity projectEntity) {
        Tuple params = Tuple.of(projectEntity.getProjectId());

        projectPool.preparedQuery(SqlQueries.getRetrieveProjectById())
                .execute(params)
                .onComplete(res -> {
                    if (res.succeeded()) {
                        log.info("Get Project: " + projectEntity.getProjectName());
                    }

                    else if (res.failed()) {
                        log.info(res.cause().getMessage());
                    }
                });
        return toProjectDTO(projectEntity);
    }

    @Override
    public ProjectDTO updateProject(@NonNull ProjectEntity projectEntity) {
        Tuple params = Tuple.of(projectEntity.getProjectId(), projectEntity.getProjectName(), projectEntity.getProjectType(),
                projectEntity.getProjectPath(), projectEntity.getProjectInfra(), projectEntity.getLabelList());

        projectPool.preparedQuery(SqlQueries.getUpdateProject())
                .execute(params)
                .onComplete(res -> {
                    if (res.succeeded()) {
                        log.info("Project " + projectEntity.getProjectName() + " updated");
                    }

                    else if (res.failed()) {
                        log.info(res.cause().getMessage());
                    }
                });

        return toProjectDTO(projectEntity);
    }

    @Override
    public ProjectDTO deleteProjectById(@NonNull ProjectEntity projectEntity) {
        Tuple params = Tuple.of(projectEntity.getProjectId());

        projectPool.preparedQuery(SqlQueries.getUpdateProject())
                .execute(params)
                .onComplete(res -> {
                    if (res.succeeded()) {
                        log.info("Delete project: " + projectEntity.getProjectName());
                    }

                    else if (res.failed()) {
                        log.info(res.cause().getMessage());
                    }
                });
        return toProjectDTO(projectEntity);
    }

}
