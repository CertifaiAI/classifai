package ai.classifai.database;

import ai.classifai.core.entity.dto.generic.*;
import ai.classifai.core.entity.dto.image.ImageDataDTO;
import ai.classifai.core.entity.dto.image.ImageDataVersionDTO;
import ai.classifai.core.entity.dto.image.annotation.ImageAnnotationDTO;
import ai.classifai.core.entity.model.generic.*;
import ai.classifai.core.entity.model.image.ImageData;
import ai.classifai.core.entity.model.image.ImageDataVersion;
import ai.classifai.core.entity.model.image.annotation.ImageAnnotation;
import ai.classifai.core.service.generic.*;
import ai.classifai.core.service.image.ImageDataRepository;
import ai.classifai.core.service.image.annotation.BoundingBoxAnnotationRepository;
import ai.classifai.core.service.image.annotation.ImageAnnotationRepository;
import ai.classifai.core.service.image.annotation.PolygonAnnotationRepository;
import ai.classifai.core.service.image.ImageDataVersionRepository;
import ai.classifai.database.entity.generic.DataVersionKey;
import ai.classifai.database.repository.generic.LabelHibernateRepository;
import ai.classifai.database.repository.generic.PointHibernateRepository;
import ai.classifai.database.repository.generic.ProjectHibernateRepository;
import ai.classifai.database.repository.generic.VersionHibernateRepository;
import ai.classifai.database.repository.image.ImageDataHibernateRepository;
import ai.classifai.database.repository.image.ImageDataVersionHibernateRepository;
import ai.classifai.database.repository.image.annotation.BoundingBoxHibernateRepository;
import ai.classifai.database.repository.image.annotation.PolygonHibernateRepository;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DbService
{
    private Vertx vertx;
    private EntityManager em;

    private VersionRepository versionRepository;
    private PointRepository pointRepository;
    private LabelRepository labelRepository;
    private ProjectRepository projectRepository;
    private ImageDataRepository imageDataRepository;
    private ImageDataVersionRepository imageDataVersionRepository;
    private BoundingBoxAnnotationRepository boundingBoxAnnotationRepository;
    private PolygonAnnotationRepository polygonAnnotationRepository;




    public DbService(Vertx vertx)
    {
        this.vertx = vertx;
        connectDatabase();

        versionRepository = new VersionHibernateRepository(em);
        pointRepository = new PointHibernateRepository(em);
        labelRepository = new LabelHibernateRepository(em);

        projectRepository = new ProjectHibernateRepository(em);
        imageDataRepository = new ImageDataHibernateRepository(em);
        imageDataVersionRepository = new ImageDataVersionHibernateRepository(em);
        boundingBoxAnnotationRepository = new BoundingBoxHibernateRepository(em);
        polygonAnnotationRepository = new PolygonHibernateRepository(em);
    }

    private void connectDatabase()
    {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("database");
        em = emf.createEntityManager();
    }

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

    private Void createImageProjectRunnable(ProjectDTO projectDTO, VersionDTO versionDTO, List<ImageDataDTO> dataDTOList, List<LabelDTO> labelDTOList)
    {
        Project project = projectRepository.create(projectDTO);
        UUID projectId = project.getId();

        versionDTO.setProjectId(projectId);
        Version version = versionRepository.create(versionDTO);
        UUID versionId = version.getId();

        projectRepository.setCurrentVersion(project, version);

        dataDTOList.forEach(imageDataDTO -> imageDataDTO.setProjectId(projectId));

        List<ImageData> dataList = dataDTOList.stream()
                .map(imageDataRepository::create)
                .collect(Collectors.toList());

        dataList.stream()
                .map(imageData -> ImageDataVersionDTO.builder()
                        .dataId(imageData.getId())
                        .versionId(versionId)
                        .build())
                .forEach(imageDataVersionRepository::create);

        labelDTOList.forEach(labelDTO ->
        {
            labelDTO.setVersionId(versionId);
            labelRepository.create(labelDTO);
        });

        return null;
    }

    public Future<Void> createImageProject(ProjectDTO projectDTO, VersionDTO versionDTO, List<ImageDataDTO> imageDataDTOList, List<LabelDTO> labelDTOList)
    {
        return runTransaction(() -> createImageProjectRunnable(projectDTO, versionDTO, imageDataDTOList, labelDTOList));
    }

    public Future<Void> starProjectFuture(Project project, Boolean isStarred)
    {
        return runTransaction(() -> projectRepository.star(project, isStarred)).mapEmpty();
    }

    public Future<Void> renameProject(Project project, String newProjectName)
    {
        return runTransaction(() -> projectRepository.rename(project, newProjectName)).mapEmpty();
    }

    public Future<Void> deleteProjectFuture(Project project)
    {
        return runTransaction(() ->
        {
            projectRepository.delete(project);
            return null;
        }).mapEmpty();
    }

    public Future<ImageData> getImageDataById(UUID id)
    {
        return vertx.executeBlocking(promise ->
                promise.complete(imageDataRepository.get(id)));
    }

    public Future<ImageDataVersion> getImageDataVersionById(UUID dataId, UUID versionId)
    {
        return vertx.executeBlocking(promise ->
                promise.complete(imageDataVersionRepository.get(new DataVersionKey(dataId, versionId))));
    }

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
        });
    }
}
