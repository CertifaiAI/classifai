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
package ai.classifai.database.loader;

import ai.classifai.annotation.AnnotationType;
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

    private Set<Integer> sanityUUIDSet;

    @Getter @Setter private List<Integer> sanityUUIDList;
    @Getter @Setter private List<String> labelList;

    private Integer currentProcessedLength;
    private Integer totalUUIDSize; //only used when going through uuid for valid path

    private AnnotationType annotationType; //TODO

    @Setter @Getter private List<Integer> progressUpdate;

    public ProjectLoader()
    {
        loaderStatus = LoaderStatus.DID_NOT_INITIATED;

        progressUpdate = new ArrayList<>(Arrays.asList(0, 1)); //temporary fix to prevent frontend display nan

        currentProcessedLength = 0;
        totalUUIDSize = -1;
        sanityUUIDSet = new HashSet<>();
        sanityUUIDList = new ArrayList<>();
        labelList = new ArrayList<>();
    }

    public List<Integer> getProgress()
    {
        List<Integer> progressBar = new ArrayList<>();

        progressBar.add(currentProcessedLength);
        progressBar.add(totalUUIDSize);

        return progressBar;
    }

    public void setTotalUUIDSize(Integer totalUUIDSizeBuffer)
    {
        totalUUIDSize = totalUUIDSizeBuffer;

        if(totalUUIDSize == 0)
        {
            loaderStatus = LoaderStatus.EMPTY;
        }
        else if(totalUUIDSize < 0)
        {
            loaderStatus = LoaderStatus.ERROR;
        }

    }

    public void updateProgress(Integer index)
    {
        currentProcessedLength = index;

        //if done, offload set to list
        if ((index + 1) == totalUUIDSize)
        {
            getUpdatedUUIDList();
            loaderStatus = LoaderStatus.LOADED;
        }
    }

    public void pushToUUIDSet(Integer uuid)
    {
        sanityUUIDSet.add(uuid);
    }

    public void getUpdatedUUIDList()
    {
        sanityUUIDList = new ArrayList<>(sanityUUIDSet);
        sanityUUIDSet.clear();

        totalUUIDSize = -1;
    }
}
