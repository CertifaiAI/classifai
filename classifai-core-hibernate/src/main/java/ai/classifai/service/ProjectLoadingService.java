package ai.classifai.service;

import ai.classifai.core.entity.model.generic.Project;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProjectLoadingService extends AbstractVertxService
{
    private final List<UUID> loadedProjectId;

    public ProjectLoadingService(Vertx vertx)
    {
        super(vertx);
        loadedProjectId = new ArrayList<>();
    }

    private Boolean isProjectLoaded(Project project)
    {
        return loadedProjectId.contains(project.getId());
    }

    private void addProjectToLoadedList(Project project)
    {
        loadedProjectId.add(project.getId());
    }

    private void removeLoadedProject(Project project)
    {
        loadedProjectId.remove(project.getId());
    }

    public Future<List<Boolean>> stateProjectsLoaded(List<Project> projectList)
    {
        List<Boolean> projectLoadedList = new ArrayList<>();

        projectList.forEach(project -> projectLoadedList.add(isProjectLoaded(project)));

        return Future.succeededFuture(projectLoadedList);

    }

    public Future<Boolean> stateProjectLoaded(Project project)
    {
        return Future.succeededFuture(isProjectLoaded(project));
    }

    public Future<Void> addToLoadedList(Project project)
    {
        return vertx.executeBlocking(promise ->
        {
            loadedProjectId.add(project.getId());
            promise.complete();
        });
    }

    public Future<Void> removeFromLoadedList(Project project)
    {
        removeLoadedProject(project);
        return Future.succeededFuture();
    }
}
