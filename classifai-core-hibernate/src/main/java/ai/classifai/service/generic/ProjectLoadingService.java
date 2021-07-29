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
package ai.classifai.service.generic;

import ai.classifai.core.entity.model.generic.Project;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.*;

/**
 * class for handling project loading feature
 *
 * @author YinChuangSum
 */
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
