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
package ai.classifai.view.annotation;


import ai.classifai.core.entity.dto.generic.AnnotationDTO;
import ai.classifai.core.entity.dto.generic.PointDTO;
import ai.classifai.core.entity.dto.image.annotation.PolygonAnnotationDTO;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * class for handling polygon annotation request and response
 *
 * @author YinChuangSum
 */
public class PolygonAnnotationView extends AnnotationView
{

    public PolygonAnnotationView(AnnotationType annotationType)
    {
        super(annotationType);
    }

    @Override
    protected JsonObject getSingleAnnotationPointView(List<PointDTO> pointDTOList)
    {
        JsonArray pointListView = new JsonArray();

        pointDTOList.forEach(pointDTO -> pointListView.add(pointToJson(pointDTO)));

        return new JsonObject()
                .put("coorPt", pointListView);
    }

    @Override
    protected List<PointDTO> decodePointLists(JsonObject view)
    {
        List<PointDTO> pointDTOList = new ArrayList<>();

        JsonArray pointViewList = view.getJsonArray("coorPt");

        IntStream.range(0, pointViewList.size())
                .forEach(idx -> {
                        JsonObject pointView = pointViewList.getJsonObject(idx);

                    JsonObject distanceToImg = pointView.getJsonObject("distancetoImg");

                    PointDTO pointDTO = PointDTO.builder()
                            .x(pointView.getFloat("x"))
                            .y(pointView.getFloat("y"))
                            .dist2ImgX(distanceToImg.getFloat("x"))
                            .dist2ImgY(distanceToImg.getFloat("y"))
                            .position(idx)
                            .build();

                    pointDTOList.add(pointDTO);
                });

        return pointDTOList;
    }

    @Override
    protected AnnotationDTO decodeAnnotation(JsonObject view)
    {
        return PolygonAnnotationDTO.builder()
                .id(view.getLong("id"))
                .build();
    }

    private JsonObject pointToJson(PointDTO pointDTO)
    {
        JsonObject distanceToImg = new JsonObject()
                .put("x", pointDTO.getDist2ImgX())
                .put("y", pointDTO.getDist2ImgY());

        return new JsonObject()
                .put("x", pointDTO.getX())
                .put("y", pointDTO.getY())
                .put("distancetoImg", distanceToImg);
    }
}
