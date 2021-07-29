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
package ai.classifai.view.dataversion;

import ai.classifai.core.entity.dto.generic.DataVersionDTO;
import ai.classifai.core.entity.dto.image.ImageDataVersionDTO;
import io.vertx.core.json.JsonObject;

/**
 * class for handling image data version request and response
 *
 * @author YinChuangSum
 */
public class ImageDataVersionView extends DataVersionView
{
    @Override
    public JsonObject generateImageDataVersionView(DataVersionDTO dataVersionDTO)
    {
        ImageDataVersionDTO dto = ImageDataVersionDTO.toDTOImpl(dataVersionDTO);

        return new JsonObject()
                .put("img_x", dto.getImgX())
                .put("img_y", dto.getImgY())
                .put("img_w", dto.getImgW())
                .put("img_h", dto.getImgH());
    }

    @Override
    public void decode(JsonObject view)
    {
        dto = ImageDataVersionDTO.builder()
                .imgX(view.getFloat("img_x"))
                .imgY(view.getFloat("img_y"))
                .imgW(view.getFloat("img_w"))
                .imgH(view.getFloat("img_h"))
                .build();
    }
}
