/*
 * Copyright (c) 2021 CertifAI Sdn. Bhd.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.classifai.database;

import ai.classifai.core.entity.dto.generic.*;
import ai.classifai.core.entity.dto.image.ImageDataVersionDTO;
import ai.classifai.core.entity.model.generic.*;
import ai.classifai.core.service.generic.*;
import ai.classifai.database.entity.generic.DataVersionKey;
import ai.classifai.database.repository.generic.*;
import ai.classifai.database.repository.image.ImageDataHibernateRepository;
import ai.classifai.database.repository.image.ImageDataVersionHibernateRepository;
import ai.classifai.database.repository.image.annotation.BoundingBoxAnnotationHibernateRepository;
import ai.classifai.database.repository.image.annotation.PolygonAnnotationHibernateRepository;
import ai.classifai.util.strategy.DataDTOEnum;
import ai.classifai.util.strategy.DataEntityEnum;
import ai.classifai.util.strategy.ImageAnnotationDTOEnum;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Class for database service with hibernate implementation
 *
 * @author YinChuangSum
 */
public class DbService
{
    private final Vertx vertx;
    private EntityManager em;

    private final VersionRepository versionRepository;
    private final PointRepository pointRepository;
    private final LabelRepository labelRepository;
    private final ProjectRepository projectRepository;
    private final DataVersionRepository dataVersionRepository;
    private final DataRepository dataRepository;
    private final AnnotationRepository annotationRepository;

    public DbService(Vertx vertx)
    {
        this.vertx = vertx;
        connectDatabase();

        versionRepository = new VersionHibernateRepository(em);
        pointRepository = new PointHibernateRepository(em);
        labelRepository = new LabelHibernateRepository(em);
        projectRepository = new ProjectHibernateRepository(em);
        dataRepository = new DataHibernateRepository(em);
        dataVersionRepository = new DataVersionHibernateRepository(em);
        annotationRepository = new AnnotationHibernateRepository(em);
    }

    private void connectDatabase()
    {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("database");
        em = emf.createEntityManager();
    }

    //**********************************PROJECT*******************************************

    public Future<List<Project>> listProjectsByAnnotation(AnnotationType annotationType)
    {
        return vertx.executeBlocking(promise ->
                promise.complete(projectRepository.listByType(annotationType.ordinal())));
    }

    public Future<Project> getProjectByNameAndAnnotation(String projectName, AnnotationType annotationType)
    {
        return vertx.<Optional<Project>>executeBlocking(promise ->
                promise.complete(projectRepository.findByNameAndType(projectName, annotationType.ordinal())))
                .compose(optional ->
                {
                    if (optional.isEmpty())
                    {
                        return Future.failedFuture(String.format("Unable to find project [%s] %s", annotationType.name(), projectName));
                    }

                    return Future.succeededFuture(optional.get());
                });
    }

    public Future<Void> projectNameAndAnnotationAvailable(String projectName, AnnotationType annotationType) 
    {
        return vertx.<Optional<Project>>executeBlocking(promise ->
                promise.complete(projectRepository.findByNameAndType(projectName, annotationType.ordinal())))
                .compose(optional ->
                {
                    if (optional.isEmpty())
                    {
                        return Future.succeededFuture();
                    }

                    return Future.failedFuture(String.format("Project [%s] %s existed!", annotationType.name(), projectName));
                });
    }

    public Project createProjectRunnable(ProjectDTO projectDTO, VersionDTO versionDTO, List<DataDTO> dataDTOList, List<LabelDTO> labelDTOList)
    {
        Project project = projectRepository.create(projectDTO);
        UUID projectId = project.getId();

        versionDTO.setProjectId(projectId);
        Version version = versionRepository.create(versionDTO);

        projectRepository.setCurrentVersion(project, version);

        dataDTOList.forEach(dataDTO -> dataDTO.setProjectId(projectId));

        List<Data> dataList = dataDTOList.stream()
                .map(this::persistData)
                .collect(Collectors.toList());

        persistDataVersionList(dataList, Collections.singletonList(version));

        labelDTOList.forEach(labelDTO ->
        {
            labelDTO.setVersionId(version.getId());
            labelRepository.create(labelDTO);
        });

        return project;
    }

    public Future<Project> createProject(ProjectDTO projectDTO, VersionDTO versionDTO, List<DataDTO> dataDTOList, List<LabelDTO> labelDTOList)
    {
        return runTransaction(() -> createProjectRunnable(projectDTO, versionDTO, dataDTOList, labelDTOList));
    }

    public Future<Project> starProject(Project project, Boolean isStarred)
    {
        return runTransaction(() -> projectRepository.star(project, isStarred));
    }

    public Future<Project> renameProject(Project project, String newProjectName)
    {
        return runTransaction(() -> projectRepository.rename(project, newProjectName));
    }

    public Future<Void> deleteProject(Project project)
    {
        return runTransaction(() ->
                projectRepository.delete(project));
    }

    public Future<Project> setProjectIsNew(Project project, boolean isNew)
    {
        return runTransaction(() -> projectRepository.setNew(project, isNew));
    }

    //**********************************DATA*******************************************

    public Future<Data> getDataById(UUID id)
    {
        return vertx.executeBlocking(promise ->
                promise.complete(dataRepository.get(id)));
    }

    private List<Data> addDataListRunnable(List<DataDTO> dataDTOList, Project project)
    {
        dataDTOList.forEach(dataDTO -> dataDTO.setProjectId(project.getId()));

        List<Data> dataList = dataDTOList.stream()
                .map(this::persistData)
                .collect(Collectors.toList());

        List<Version> versionList = project.getVersionList();

        persistDataVersionList(dataList, versionList);

        return dataList;
    }

    public Future<List<Data>> addDataList(List<DataDTO> dataDTOList, Project project)
    {
        return runTransaction(() -> addDataListRunnable(dataDTOList, project));
    }

    //**********************************DATA VERSION*******************************************

    public Future<DataVersion> getDataVersionById(UUID dataId, UUID versionId)
    {   return vertx.executeBlocking(promise ->
                promise.complete(dataVersionRepository.get(new DataVersionKey(dataId, versionId))));
    }

    public Future<DataVersion> updateDataVersion(DataVersion dataVersion, DataVersionDTO dto)
    {
        return vertx.executeBlocking(promise ->
                promise.complete(dataVersionRepository.update(dataVersion, dto)));
    }

    private void persistDataVersionList(List<Data> dataList, List<Version> versionList)
    {
        dataList.forEach(data -> versionList
                .forEach(version -> persistDataVersion(data, version)));
    }

    //**********************************LABEL*******************************************

    public Future<List<Label>> listLabelByVersion(Version version)
    {
        return vertx.executeBlocking(promise ->
                promise.complete(labelRepository.listByVersion(version)));
    }

    public Future<Void> deleteLabelList(List<Label> labelList)
    {
        return runTransaction(() ->
                labelList.stream()
                        .map(Label::getId)
                        .forEach(this::deleteLabelById));
    }

    public Future<Void> deleteLabelById(UUID labelId)
    {
        return runTransaction(() ->
        {
            Label label = labelRepository.get(labelId);
            labelRepository.delete(label);
        });
    }

    public Future<Void> addLabelList(List<LabelDTO> labelDTOList)
    {
        return runTransaction(() ->
                labelDTOList.forEach(labelRepository::create));
    }

    //**********************************ANNOTATION*******************************************

    public Annotation addImageAnnotationRunnable(AnnotationDTO annotationDTO, List<PointDTO> pointDTOList)
    {
        Annotation annotation = persistImageAnnotation(annotationDTO);

        pointDTOList.forEach(pointDTO ->
        {
            pointDTO.setAnnotationId(annotation.getId());
            pointRepository.create(pointDTO);
        });

        return annotation;
    }


    public Future<Annotation> addImageAnnotation(AnnotationDTO annotationDTO, List<PointDTO> pointDTOList)
    {
        return runTransaction(() -> addImageAnnotationRunnable(annotationDTO, pointDTOList));
    }

    public Future<Void> deleteAnnotation(Annotation annotation)
    {
        return runTransaction(() ->
                annotationRepository.delete(annotation));
    }

    public Future<Annotation> setAnnotationLabel(Annotation annotation, Label label)
    {
        return runTransaction(() -> annotationRepository.setLabel(annotation, label));
    }

    //**********************************POINT*******************************************

    public Future<Point> updatePoint(Point point, PointDTO dto)
    {
        return runTransaction(() -> pointRepository.update(point, dto));
    }

    //**********************************UTIL*******************************************

    private <T> void dbErrorHandler(Promise<T> promise, Throwable throwable)
    {
        em.getTransaction().rollback();
        promise.fail(String.format("Database error: %s", throwable.getMessage()));
    }

    // unit of work
    private <T> Future<T> runTransaction(Supplier<T> supplier)
    {
        return vertx.executeBlocking(promise ->
        {
            try
            {
                em.getTransaction().begin();

                T result = supplier.get();

                em.getTransaction().commit();

                promise.complete(result);
            }
            catch (Exception e)
            {
                dbErrorHandler(promise, e.getCause());
            }
            finally
            {
                em.clear();
            }
        });
    }

    private Future<Void> runTransaction(Runnable runnable)
    {
        return vertx.executeBlocking(promise ->
        {
            try
            {
                em.getTransaction().begin();

                runnable.run();

                em.getTransaction().commit();

                promise.complete();
            }
            catch (Exception e)
            {
                dbErrorHandler(promise, e.getCause());
            }
            finally
            {
                em.clear();
            }
        });
    }

    //**********************************STRATEGY*******************************************

    private Annotation persistImageAnnotation(AnnotationDTO annotationDTO)
    {
        ImageAnnotationDTOEnum imageAnnotationDTOEnum = ImageAnnotationDTOEnum.valueOf(annotationDTO.getClass().getSimpleName().toUpperCase(Locale.ROOT));

        return switch (imageAnnotationDTOEnum)
        {
            case BOUNDINGBOXANNOTATIONDTO -> new BoundingBoxAnnotationHibernateRepository(em).create(annotationDTO);

            case POLYGONANNOTATIONDTO -> new PolygonAnnotationHibernateRepository(em).create(annotationDTO);
        };
    }

    private void persistDataVersion(Data data, Version version)
    {
        DataEntityEnum dataEntityEnum = DataEntityEnum.valueOf(data.getClass().getSimpleName().toUpperCase(Locale.ROOT));

        switch (dataEntityEnum)
        {
            case IMAGEDATAENTITY -> {
                ImageDataVersionDTO dto = ImageDataVersionDTO.builder()
                        .dataId(data.getId())
                        .versionId(version.getId())
                        .build();
                new ImageDataVersionHibernateRepository(em).create(dto);
            }
        }
    }

    private Data persistData(DataDTO dataDTO)
    {
        DataDTOEnum dtoEnum = DataDTOEnum.valueOf(dataDTO.getClass().getSimpleName().toUpperCase(Locale.ROOT));

        return switch(dtoEnum)
        {
            case IMAGEDATADTO -> new ImageDataHibernateRepository(em).create(dataDTO);
        };
    }
}
