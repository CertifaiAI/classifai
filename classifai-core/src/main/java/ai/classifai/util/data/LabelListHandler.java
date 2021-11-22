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
import ai.classifai.loader.ProjectLoader;
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

        // To get a list of JsonArray, whereby each JsonArray contain annotation parameters of an image
        List<JsonArray> labelPointData = annotationList.stream()
                .map(Annotation::getAnnotationDictDbFormat)
                .map(LabelListHandler::getAnnotationData)
                .map(Map::values)
                .map(String::valueOf)
                .map(LabelListHandler::getAnnotationStatus)
                .collect(Collectors.toList());

        Predicate<JsonArray> isEmpty = JsonArray::isEmpty;
        Predicate<JsonArray> notEmpty = isEmpty.negate();

        // Checking If a JsonArray is not empty, annotation was performed on an image
        List<JsonArray> labeledImageList = labelPointData.stream()
                .filter(notEmpty)
                .collect(Collectors.toList());

        // Checking if a JsonArray is empty, annotation was not performed on an image
        List<JsonArray> unlabeledImageList = labelPointData.stream()
                .filter(isEmpty)
                .collect(Collectors.toList());

        totalImage.add(0, labeledImageList);
        totalImage.add(1, unlabeledImageList);

    }

    public static Integer getNumberOfLabeledImage() { return totalImage.get(0).size(); }

    public static Integer getNumberOfUnLabeledImage()
    {
        return totalImage.get(1).size();
    }

    // To extract the annotation data and version uuid of an Image
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

        // To get the data : annotation, img_x, img_y, img_w, img_h
        JsonObject annotationDataJsonObject = new JsonObject(s);

        // To get annotation parameters : x1, y1, x2, y2, color, distToImg, label ,id
        return annotationDataJsonObject.getJsonArray(ParamConfig.getAnnotationParam());

    }

    public static List<LinkedHashMap<String, String>> getLabelPerClassInProject(Map<String, Annotation> uuidAnnotationDict,
                                                                          ProjectLoader projectLoader)
    {
        Set<String> imageUUID = uuidAnnotationDict.keySet();
        List<Annotation> annotationList = getAnnotationList(imageUUID, uuidAnnotationDict);
        List<LinkedHashMap<String, String>> labelPerClassInProjectList = new ArrayList<>();

        // To get list of map whereby each map represent a label and its number of occurrence on an Image
        List<Map<String, Integer>> labelByClassList = annotationList.stream()
                .map(Annotation::getAnnotationDictDbFormat)
                .map(LabelListHandler::getAnnotationData)
                .map(Map::values)
                .map(String::valueOf)
                .map(LabelListHandler::getAnnotationStatus)
                .map(LabelListHandler::getLabelByClass)
                .collect(Collectors.toList());

        // Collect all the labels that not used in annotation
        List<Map<String, Integer>> unUsedLabelList = getUnUsedLabelList(labelByClassList, projectLoader);

        labelByClassList.addAll(unUsedLabelList);

        // To sum all occurrences of each label of respective class in the project
        Map<String,Integer> sumLabelByClass = labelByClassList.stream()
                .flatMap(m -> m.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Integer::sum));

       sumLabelByClass.entrySet().stream()
                .map(m -> getMap(m.getKey(), m.getValue()))
                .forEach(labelPerClassInProjectList::add);

        return labelPerClassInProjectList;

    }

    private static LinkedHashMap<String, String> getMap(String key, Integer value)
    {
        LinkedHashMap<String, String> labelCountMap = new LinkedHashMap<>();
        labelCountMap.put(ParamConfig.getLabelParam(), key);
        labelCountMap.put(ParamConfig.getLabelCountParam(), String.valueOf(value));

        return labelCountMap;
    }

    private static Map<String,Integer> getLabelByClass(JsonArray labelPointData)
    {
        Map<String,Integer> labelByClass = new HashMap<>();

        // To get a list label on an Image
        List<String> labels = IntStream.range(0, labelPointData.size())
                .mapToObj(labelPointData::getJsonObject)
                .map(m -> m.getString("label"))
                .collect(Collectors.toList());

        // To get each label with its occurrence into a map
        Consumer<String> action = s -> labelByClass.put(s, Collections.frequency(labels, s));

        labels.forEach(action);

        return labelByClass;

    }

    private static List<Annotation> getAnnotationList(Set<String> imageUUID, Map<String, Annotation> uuidAnnotationDict)
    {
        return imageUUID.stream().map(uuidAnnotationDict::get).collect(Collectors.toList());
    }

    private static List<Map<String, Integer>> getUnUsedLabelList (List<Map<String, Integer>> labelByClassList,
                                                                  ProjectLoader projectLoader)
    {
        List<String> originalLabelList = projectLoader.getLabelList();
        Map<String, Integer> unUsedLabels = new HashMap<>();
        List<Map<String, Integer>> unUsedLabelList = new ArrayList<>();

        // To get a list of label that used in annotation
        List<String> usedLabel = labelByClassList.stream()
                .flatMap(m -> m.entrySet().stream())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // To filter out the unused label from original label list
        List<String> filterList = originalLabelList.stream()
                .filter(s -> !usedLabel.contains(s))
                .collect(Collectors.toList());

        for(String label : filterList){
            unUsedLabels.put(label, 0);
            unUsedLabelList.add(unUsedLabels);
        }

        return unUsedLabelList;
    }


}
