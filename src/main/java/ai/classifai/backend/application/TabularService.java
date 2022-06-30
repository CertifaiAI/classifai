package ai.classifai.backend.application;

import ai.classifai.backend.repository.query.TabularAnnotationQuery;
import ai.classifai.core.data.handler.TabularHandler;
import ai.classifai.core.dto.ProjectDTO;
import ai.classifai.core.dto.TabularDTO;
import ai.classifai.core.entity.annotation.TabularEntity;
import ai.classifai.core.loader.ProjectLoader;
import ai.classifai.core.loader.ProjectLoaderStatus;
import ai.classifai.core.properties.tabular.TabularProperties;
import ai.classifai.core.service.annotation.AnnotationRepository;
import ai.classifai.core.service.annotation.AnnotationService;
import ai.classifai.core.service.project.ProjectService;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class TabularService implements AnnotationService<TabularDTO, TabularProperties> {

    private final ProjectService projectService;
    private final TabularHandler tabularHandler = new TabularHandler();
    private final AnnotationRepository<TabularEntity, TabularDTO> tabularRepoService;

    public TabularService(AnnotationRepository<TabularEntity, TabularDTO> tabularRepoService,
                          ProjectService projectService) {
        this.tabularRepoService = tabularRepoService;
        this.projectService = projectService;
    }

    @Override
    public Future<ProjectLoader> createAnnotationProject(ProjectDTO projectDTO) throws Exception {
        Promise<ProjectLoader> promise = Promise.promise();
        tabularHandler.parseFile(projectDTO);
        Map<String, String> headers = tabularHandler.getHeaders();
        TabularAnnotationQuery.createProjectTablePreparedStatement(headers, projectDTO.getProjectName());
        return promise.future();
    }

    @Override
    public Future<TabularDTO> createAnnotation(TabularDTO annotationDTO) throws Exception {
        return null;
    }

    @Override
    public Future<List<TabularDTO>> listAnnotations(String projectName) {
        return null;
    }

    @Override
    public Future<Optional<TabularDTO>> getAnnotationById(String projectName, String uuid) {
        return null;
    }

    @Override
    public Future<Void> updateAnnotation(@NonNull TabularDTO annotationDTO, @NonNull ProjectLoader projectLoader) {
        return null;
    }

    @Override
    public Future<Void> deleteData(@NonNull String projectName, @NonNull String uuid) {
        return null;
    }

    @Override
    public Future<Void> deleteProjectById(@NonNull ProjectDTO projectDTO) {
        return null;
    }

    @Override
    public Future<ProjectLoaderStatus> loadProject(ProjectLoader projectLoader) {
        return null;
    }

    @Override
    public Future<String> renameData(@NonNull ProjectLoader projectLoader, String uuid, String newFileName) {
        return null;
    }

    @Override
    public TabularDTO toDTO(TabularProperties property, @NonNull ProjectLoader loader) {
        return null;
    }
}
