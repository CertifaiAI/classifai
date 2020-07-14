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

package ai.classifai.data;

import ai.classifai.annotation.AnnotationType;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class DataCollection<T>
{
    @Getter
    protected Map<Integer, String> UUIDtoPath;

    @Getter
    protected Map<Integer, T> dataDict;

    protected String rootDataPath;
    protected DataType dataType;
    protected AnnotationType annotationType;

    public DataCollection(@NonNull String dataPath, DataType dataType, AnnotationType annotationType)
    {
        this.rootDataPath = dataPath;
        this.dataType = dataType;
        this.annotationType = annotationType;

        this.dataDict = new HashMap<>();
        this.UUIDtoPath = new HashMap<>();
    }

    protected abstract void createDataDict();

    public abstract void printDataDict();
}
