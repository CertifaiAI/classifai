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

import ai.classifai.core.entity.dto.generic.DataDTO;
import ai.classifai.core.entity.model.generic.Project;
import ai.classifai.core.entity.model.generic.Data;
import ai.classifai.service.image.ImageDataService;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Abstract class for data handling
 *
 * @author YinChuangSum
 */
public abstract class DataService extends FileService
{
    public DataService(Vertx vertx) {
        super(vertx);
    }

    public static DataService getDataService(AnnotationType annotationType, Vertx vertx)
    {
        return switch(annotationType)
        {
            case BOUNDINGBOX, SEGMENTATION -> new ImageDataService(vertx);
        };
    }

    public abstract Future<List<DataDTO>> getDataDTOList(String path);

    private Boolean isDataValid(Data data)
    {
        return isPathExists(data.getPath()) && isChecksumMatch(data.getChecksum(), data.getPath());
    }

    public Future<List<Data>> filterValidData(List<Data> dataList)
    {
        return Future.succeededFuture(dataList.stream()
                .filter(this::isDataValid)
                .collect(Collectors.toList()));
    }

    public abstract Future<List<DataDTO>> getToAddDataDtoList(List<Data> dataList, Project result);
}
