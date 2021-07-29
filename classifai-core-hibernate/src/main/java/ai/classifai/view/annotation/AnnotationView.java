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
import ai.classifai.core.entity.dto.generic.LabelDTO;
import ai.classifai.core.entity.dto.generic.PointDTO;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * abstract class for handling annotation request and response
 *
 * @author YinChuangSum
 */
@Getter
public abstract class AnnotationView
{
    final AnnotationType annotationType;
    protected List<AnnotationDTO> annotationDTOList;
    protected List<List<PointDTO>> pointDTOLists;
    protected List<LabelDTO> labelDTOList;

    protected AnnotationView(AnnotationType annotationType)
    {
        this.annotationType = annotationType;
    }

    public static AnnotationView getAnnotationView(AnnotationType annotationType)
    {
        return switch(annotationType)
        {
            case BOUNDINGBOX -> new BoundingBoxAnnotationView(annotationType);
            case SEGMENTATION -> new PolygonAnnotationView(annotationType);
        };
    }

    public JsonObject generateAnnotationView(List<AnnotationDTO> imageAnnotationDTOList, List<List<PointDTO>> pointDTOLists, List<LabelDTO> labelDTOList)
    {
        JsonArray annotationList = new JsonArray();

        IntStream.range(0, imageAnnotationDTOList.size())
                .forEach(i -> annotationList.add(getSingleAnnotationView(imageAnnotationDTOList.get(i), pointDTOLists.get(i), labelDTOList.get(i))));

        return new JsonObject()
                .put(annotationType.metaKey, annotationList);
    }

    private JsonObject getSingleAnnotationView(AnnotationDTO annotationDTO, List<PointDTO> pointDTOList, LabelDTO labelDTO)
    {
        return getSingleAnnotationBasicView(annotationDTO, labelDTO)
                .mergeIn(getSingleAnnotationPointView(pointDTOList));
    }

    protected abstract JsonObject getSingleAnnotationPointView(List<PointDTO> pointDTOList);

    private JsonObject getSingleAnnotationBasicView(AnnotationDTO annotationDTO, LabelDTO labelDTO)
    {
        return new JsonObject()
                .put("lineWidth", 1)
                .put("color", labelDTO.getColor())
                .put("label", labelDTO.getName())
                .put("id", annotationDTO.getId());
    }

    public void decode(JsonObject view)
    {
        JsonArray annotationArray = view.getJsonArray(annotationType.metaKey);

        annotationDTOList = new ArrayList<>();
        pointDTOLists = new ArrayList<>();
        labelDTOList = new ArrayList<>();

        IntStream.range(0, annotationArray.size())
                .forEach(idx ->
                {
                    JsonObject annotationView = annotationArray.getJsonObject(idx);

                    AnnotationDTO annotationDTO = decodeAnnotation(annotationView);
                    annotationDTO.setPosition(idx);
                    annotationDTOList.add(annotationDTO);
                    pointDTOLists.add(decodePointLists(annotationView));
                    labelDTOList.add(decodeLabel(annotationView));
                });
    }

    protected abstract List<PointDTO> decodePointLists(JsonObject view);

    protected abstract AnnotationDTO decodeAnnotation(JsonObject view);

    public LabelDTO decodeLabel(JsonObject view)
    {
        String name = view.getString("label");

        return LabelDTO.builder()
                .name(name)
                .build();
    }

}
