package ai.classifai.service;

import ai.classifai.core.entity.model.generic.Project;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.ArrayList;
import java.util.List;

public class ProjectService extends FileService
{
    public ProjectService(Vertx vertx)
    {
        super(vertx);
    }

    private Boolean isProjectPathValid(String path)
    {
        return isDirectory(path) && isPathExists(path);
    }

    public Future<List<Boolean>> stateProjectsPathValid(List<Project> projectList)
    {
        List<Boolean> pathValidList = new ArrayList<>();

        projectList.forEach(project ->
                pathValidList.add(isProjectPathValid(project.getPath())));

        return Future.succeededFuture(pathValidList);
    }

    public Future<Boolean> stateProjectPathValid(Project project)
    {
        return Future.succeededFuture(isProjectPathValid(project.getPath()));
    }
}
