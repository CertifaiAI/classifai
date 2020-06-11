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

package ai.certifai.data.type.image;

import ai.certifai.annotation.AnnotationType;
import ai.certifai.data.DataCollection;
import ai.certifai.data.DataType;
import ai.certifai.util.FileUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;

@Slf4j
public class ImageDataCollection extends DataCollection<ImageData>
{
    public ImageDataCollection(@NonNull String dataPath, DataType dataType, AnnotationType annotationType)
    {
        super(dataPath, dataType, annotationType);

        this.createDataDict();
    }

    protected void createDataDict()
    {
        /*
        //temporary: to always start from 0
        FileUtils.setFileCount(0);

        List<File> dataPathList = FileUtils.getFiles(this.rootDataPath, this.dataType);

        if(dataPathList.isEmpty())
        {
            log.info("No data is found!");
        }
        else
        {
            for(File dataAbsPath : dataPathList)
            {
                Integer uniqueId = FileUtils.generateUniqueId();
                this.dataDict.put(uniqueId, new ImageData(uniqueId, dataAbsPath.getAbsolutePath()));
                this.UUIDtoPath.put(uniqueId, dataAbsPath.getAbsolutePath());
            }
        }
         */
    }

    public void printDataDict()
    {
        this.dataDict.forEach((k,v) -> System.out.println("ID = " + k + ", Object = " + v.getDataAbsPath()));
    }

    public void printUUIDtoPaths()
    {
        this.UUIDtoPath.forEach((k,v) -> System.out.println("ID = " + k + ", Path = " + v));

    }
}
