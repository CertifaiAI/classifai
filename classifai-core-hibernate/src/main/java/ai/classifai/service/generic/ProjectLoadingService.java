package ai.classifai.service.generic;

import ai.classifai.core.entity.model.generic.Project;
import ai.classifai.service.generic.AbstractVertxService;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.*;

public class ProjectLoadingService extends AbstractVertxService
{
    private final Set<UUID> loadedProjectId;

    public ProjectLoadingService(Vertx vertx)
    {
        super(vertx);
        loadedProjectId = new HashSet<>();
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
        return vertx.executeBlocking(promise ->
        {
            List<Boolean> projectLoadedList = new ArrayList<>();

            projectList.forEach(project -> projectLoadedList.add(isProjectLoaded(project)));

            promise.complete(projectLoadedList);
        });
    }

    public Future<Boolean> stateProjectLoaded(Project project)
    {
        return vertx.executeBlocking(promise ->
                promise.complete(isProjectLoaded(project)));
    }

    public Future<Void> addToLoadedList(Project project)
    {
        return vertx.executeBlocking(promise ->
        {
            addProjectToLoadedList(project);
            promise.complete();
        });
    }

    public Future<Void> removeFromLoadedList(Project project)
    {
        return vertx.executeBlocking(promise ->
        {
            removeLoadedProject(project);
            promise.complete();
        });
    }
}
