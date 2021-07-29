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

import java.util.ArrayList;
import java.util.List;

/**
 * class for project handling
 *
 * @author YinChuangSum
 */
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
