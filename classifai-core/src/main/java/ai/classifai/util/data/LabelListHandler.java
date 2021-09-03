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
package ai.classifai.util.data;

import ai.classifai.database.versioning.Annotation;
import ai.classifai.util.ParamConfig;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Getting information of labeled and unlabeled image
 *
 * @author ken479
 */

@Slf4j
public class LabelListHandler {

    private LabelListHandler()
    {
        throw new IllegalStateException("Utility class");
    }

    private static final List<List<JsonArray>> totalImage = new ArrayList<>();

    // Handling the number of labeled and unlabeled image
    public static void getImageLabeledStatus(Map<String, Annotation> uuidAnnotationDict)
    {
        Set<String> imageUUID = uuidAnnotationDict.keySet(); // key for each project
        List<Annotation> annotationList = getAnnotationList(imageUUID, uuidAnnotationDict);// a list of annotation

        List<JsonArray> labelPointData = annotationList.stream()
                .map(Annotation::getAnnotationDictDbFormat)
                .map(LabelListHandler::getAnnotationData)
                .map(Map::values)
                .map(String::valueOf)
                .map(LabelListHandler::getAnnotationStatus)
                .collect(Collectors.toList());

        Predicate<JsonArray> isEmpty = JsonArray::isEmpty;
        Predicate<JsonArray> notEmpty = isEmpty.negate();

        List<JsonArray> labeledImageList = labelPointData.stream()
                .filter(notEmpty)
                .collect(Collectors.toList());

        List<JsonArray> unlabeledImageList = labelPointData.stream()
                .filter(isEmpty)
                .collect(Collectors.toList());

        totalImage.add(0, labeledImageList);
        totalImage.add(1, unlabeledImageList);

    }

    public static Integer getNumberOfLabeledImage()
    {
        return totalImage.get(0).size();
    }

    public static Integer getNumberOfUnLabeledImage()
    {
        return totalImage.get(1).size();
    }

    private static LinkedHashMap<String,JsonObject> getAnnotationData(String annotationDict)
    {
        LinkedHashMap<String, JsonObject> annotationDataMap = new LinkedHashMap<>();
        String s = StringUtils.removeStart(StringUtils.removeEnd(annotationDict, "]"), "[");

        JsonObject annotationDictJsonObject = new JsonObject(s);
        String versionUuid = annotationDictJsonObject.getString(ParamConfig.getVersionUuidParam());
        JsonObject annotationData = annotationDictJsonObject.getJsonObject(ParamConfig.getAnnotationDataParam());

        annotationDataMap.put(versionUuid, annotationData);

        return annotationDataMap;

    }

    private static JsonArray getAnnotationStatus(String annotationDictValue)
    {
        String s = StringUtils.removeStart(StringUtils.removeEnd(annotationDictValue, "]"), "[");
        JsonObject annotationDataJsonObject = new JsonObject(s); //annotation, img_x, img_y, img_w, img_h

        // To get annotation parameters
        return annotationDataJsonObject.getJsonArray(ParamConfig.getAnnotationParam());// x1, y1, x2, y2, color, distToImg, label ,id

    }

    public static JsonArray getLabelPerClassInProject(Map<String, Annotation> uuidAnnotationDict)
    {
        Set<String> imageUUID = uuidAnnotationDict.keySet();
        List<Annotation> annotationList = getAnnotationList(imageUUID, uuidAnnotationDict);
        JsonArray labelPerClassInProjectJsonArray = new JsonArray();

        List<Map<String, Integer>> labelByClassList = annotationList.stream()
                .map(Annotation::getAnnotationDictDbFormat)
                .map(LabelListHandler::getAnnotationData)
                .map(Map::values)
                .map(String::valueOf)
                .map(LabelListHandler::getAnnotationStatus)
                .map(LabelListHandler::getLabelByClass)
                .collect(Collectors.toList());

        Map<String,Integer> sumLabelByClass = labelByClassList.stream()
                .flatMap(m -> m.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Integer::sum));

       sumLabelByClass.entrySet().stream()
                .map(m -> getJsonObject(m.getKey(), m.getValue()))
                .forEach(labelPerClassInProjectJsonArray::add);

        return labelPerClassInProjectJsonArray;

    }

    private static JsonObject getJsonObject(String key, Integer value)
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put(ParamConfig.getLabelParam(), key);
        jsonObject.put(ParamConfig.getLabelCountParam(), value);

        return jsonObject;
    }

    private static Map<String,Integer> getLabelByClass(JsonArray labelPointData)
    {
        Map<String,Integer> labelByClass = new HashMap<>();

        List<String> labels = IntStream.range(0, labelPointData.size())
                .mapToObj(labelPointData::getJsonObject)
                .map(m -> m.getString("label"))
                .collect(Collectors.toList());

        Consumer<String> action = s -> {
            Integer count = labelByClass.get(s);
            if (count == null)
            {
                count = 0;
            }
            labelByClass.put(s, count + 1);
        };

        labels.forEach(action);

        return labelByClass;

    }

    private static List<Annotation> getAnnotationList(Set<String> imageUUID, Map<String, Annotation> uuidAnnotationDict)
    {
        return imageUUID.stream().map(uuidAnnotationDict::get).collect(Collectors.toList());
    }


}
