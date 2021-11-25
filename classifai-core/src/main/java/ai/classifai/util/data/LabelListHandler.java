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
import ai.classifai.dto.data.AnnotationPointProperties;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.util.ParamConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public LabelListHandler() { }

    private final List<List<List<AnnotationPointProperties>>> totalImage = new ArrayList<>();

    public void getImageLabeledStatus(Map<String, Annotation> uuidAnnotationDict)
    {
        Set<String> imageUUID = uuidAnnotationDict.keySet();
        List<Annotation> annotationList = getAnnotationList(imageUUID, uuidAnnotationDict);

        List<List<AnnotationPointProperties>> labelPointData = annotationList.stream()
                .map(Annotation::getAnnotationDictDbFormat)
                .map(LabelListHandler::getAnnotationData)
                .map(Map::values)
                .map(String::valueOf)
                .map(LabelListHandler::getAnnotationStatus)
                .collect(Collectors.toList());

        Predicate<List<AnnotationPointProperties>> isEmpty = List::isEmpty;
        Predicate<List<AnnotationPointProperties>> notEmpty = isEmpty.negate();

        List<List<AnnotationPointProperties>> labeledImageList = labelPointData.stream()
                .filter(notEmpty)
                .collect(Collectors.toList());

        List<List<AnnotationPointProperties>> unlabeledImageList = labelPointData.stream()
                .filter(isEmpty)
                .collect(Collectors.toList());

        totalImage.add(0, labeledImageList);
        totalImage.add(1, unlabeledImageList);

    }

    public Integer getNumberOfLabeledImage() { return totalImage.get(0).size(); }

    public Integer getNumberOfUnLabeledImage()
    {
        return totalImage.get(1).size();
    }

    private static LinkedHashMap<String,String> getAnnotationData(String annotationDict)
    {
        LinkedHashMap<String, String> annotationDataMap = new LinkedHashMap<>();
        String annotationDictString = StringUtils.removeStart(StringUtils.removeEnd(annotationDict, "}]"), "[{");
        String[] keyValuePairs = annotationDictString.split(",", 2);

        for(String pair : keyValuePairs)
        {
            String[] entry = pair.split(":",2);
            annotationDataMap.put(entry[0], entry[1]);
        }

        return annotationDataMap;

    }

    private static List<AnnotationPointProperties> getAnnotationStatus(String annotationDictValue)
    {
        ObjectMapper mapper = new ObjectMapper();
        AnnotationPointProperties annotationData;
        List<AnnotationPointProperties> annotationDataList = new ArrayList<>();

        String annotationDictValueString = StringUtils.removeStart(StringUtils.removeEnd(annotationDictValue, "]"), "[");
        String[] annotationValue = annotationDictValueString.split(",", 2);
        JsonObject annotationDataJsonObject = new JsonObject(annotationValue[1].trim());
        JsonArray annotationDataJsonArray = annotationDataJsonObject.getJsonArray(ParamConfig.getAnnotationParam());

        try
        {
            for (int i = 0; i < annotationDataJsonArray.size(); i++)
            {
                String jsonString = annotationDataJsonArray.getJsonObject(i).toString();
                annotationData = mapper.readValue(jsonString, AnnotationPointProperties.class);
                annotationDataList.add(annotationData);
            }
        }
        catch (JsonProcessingException e)
        {
            log.info("Fail to process annotation point properties");
        }

        return annotationDataList;

    }

    public List<LinkedHashMap<String, String>> getLabelPerClassInProject(Map<String, Annotation> uuidAnnotationDict,
                                                                          ProjectLoader projectLoader)
    {
        Set<String> imageUUID = uuidAnnotationDict.keySet();
        List<Annotation> annotationList = getAnnotationList(imageUUID, uuidAnnotationDict);
        List<LinkedHashMap<String, String>> labelPerClassInProjectList = new ArrayList<>();

        List<Map<String, Integer>> labelByClassList = annotationList.stream()
                .map(Annotation::getAnnotationDictDbFormat)
                .map(LabelListHandler::getAnnotationData)
                .map(Map::values)
                .map(String::valueOf)
                .map(LabelListHandler::getAnnotationStatus)
                .map(LabelListHandler::getLabelByClass)
                .collect(Collectors.toList());

        List<Map<String, Integer>> unUsedLabelList = getUnUsedLabelList(labelByClassList, projectLoader);

        labelByClassList.addAll(unUsedLabelList);

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

    private static Map<String,Integer> getLabelByClass(List<AnnotationPointProperties> labelPointData)
    {
        Map<String,Integer> labelByClass = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        JsonArray labelPointDataJsonArray = new JsonArray();

        for(AnnotationPointProperties annotationPointProperties : labelPointData)
        {
            try
            {
                String labelPointDataString = mapper.writeValueAsString(annotationPointProperties);
                labelPointDataJsonArray.add(new JsonObject(labelPointDataString));
            }
            catch (JsonProcessingException e)
            {
                log.info("Unable to process json annotation point properties");
            }
        }

        List<String> labels = IntStream.range(0, labelPointDataJsonArray.size())
                .mapToObj(labelPointDataJsonArray::getJsonObject)
                .map(m -> m.getString("label"))
                .collect(Collectors.toList());

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

        List<String> usedLabel = labelByClassList.stream()
                .flatMap(m -> m.entrySet().stream())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

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
