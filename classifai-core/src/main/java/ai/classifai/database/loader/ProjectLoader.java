/*System.out.println
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
package ai.classifai.database.loader;

import ai.classifai.data.DataType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Class per project for managing the loading of project
 *
 * @author Chiawei Lim
 */
@Slf4j
public class ProjectLoader
{
    @Getter @Setter private LoaderStatus loaderStatus;

    @Getter @Setter private List<String> labelList;
    @Getter private Set<Integer> sanityUUIDList;

    private Integer currentProcessedLength;
    @Setter private Integer totalUUIDSize;

    @Getter private DataType dataType = DataType.IMAGE;

    @Setter @Getter private List<Integer> progressUpdate;

    public ProjectLoader()
    {
        setProjectLoader(LoaderStatus.DID_NOT_INITIATED);

        progressUpdate = new ArrayList<>(Arrays.asList(0, 1)); //temporary fix to prevent frontend display nan
    }

    public void resetLoaderStatus()
    {
        loaderStatus = LoaderStatus.DID_NOT_INITIATED;
        currentProcessedLength = 0;
        sanityUUIDList.clear();
        labelList.clear();
    }

    private void setProjectLoader(LoaderStatus status)
    {
        loaderStatus = status;
        sanityUUIDList = new HashSet<>();
        currentProcessedLength = 0;
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
