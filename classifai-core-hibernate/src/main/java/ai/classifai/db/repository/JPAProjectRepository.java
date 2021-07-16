package ai.classifai.db.repository;

import ai.classifai.core.entities.Project;
import ai.classifai.core.entities.dto.ProjectDTO;
import ai.classifai.core.services.repository.ProjectRepository;
import ai.classifai.db.entities.ProjectEntity;
import lombok.NonNull;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JPAProjectRepository extends JPARepository<Project, ProjectDTO, UUID> implements ProjectRepository
{
    public JPAProjectRepository(EntityManager em)
    {
        super(em, ProjectEntity::fromDTO);
    }

    @Override
    public List<Project> list()
    {
        TypedQuery<ProjectEntity> query = em.createNamedQuery("ProjectEntity.listAll", ProjectEntity.class);

        return new ArrayList<>(query.getResultList());
    }

    @Override
    public Project get(@NonNull UUID id)
    {
        return em.find(ProjectEntity.class, id);
    }

    @Override
    public List<ProjectEntity> listByAnnotationType(@NonNull Integer annotationType)
    {
        return em.createNamedQuery("ProjectEntity.listByAnnotationType", ProjectEntity.class)
                .setParameter("annotationType", annotationType)
                .getResultList();
    }

    @Override
    public Optional<ProjectEntity> find(@NonNull String name, @NonNull Integer annotationType)
    {
        return em.createNamedQuery("ProjectEntity.findByNameAndAnnotationType", ProjectEntity.class)
                .setParameter("annotationType", annotationType)
                .setParameter("name", name)
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public Project rename(Project project, @NonNull String newName)
    {
        ProjectDTO dto = project.toDTO();
        dto.setName(newName);
        return update(dto);
    }

    @Override
    public Project setStarred(Project project, @NonNull Boolean starred)
    {
        ProjectDTO dto = project.toDTO();
        dto.setStarred(starred);
        return update(dto);
    }
}
