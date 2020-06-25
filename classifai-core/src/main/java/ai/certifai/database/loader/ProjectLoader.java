/*
 * Copyright (c) 2020 CertifAI
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
package ai.certifai.database.loader;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Class per project for managing the loading of project
 *
 * @author Chiawei Lim
 */
@Slf4j
public class ProjectLoader
{
    @Getter @Setter private LoaderStatus loaderStatus;
    @Getter private List<Integer> sanityUUIDList;
    private Integer currentProcessedLength;
    @Setter private Integer totalUUIDSize;

    public ProjectLoader()
    {
        setProjectLoader(LoaderStatus.DID_NOT_INITIATED);
    }

    private void setProjectLoader(LoaderStatus status)
    {
        loaderStatus = status;
        sanityUUIDList = new ArrayList<>();
        currentProcessedLength = 0;
    }

    public void resetProjectLoader(LoaderStatus status)
    {
        setProjectLoader(status);
    }

    public List<Integer> getProgress()
    {
        List<Integer> progressBar = new ArrayList<>();

        progressBar.add(currentProcessedLength);
        progressBar.add(totalUUIDSize);

        return progressBar;
    }

    public void updateSanityUUIDItem(Integer uuid)
    {
        sanityUUIDList.add(uuid);
    }

    public void updateProgress(Integer lengthNow)
    {
        currentProcessedLength = lengthNow;
    }

}
