package ai.classifai.util.data;

import ai.classifai.database.versioning.Annotation;
import ai.classifai.database.versioning.AnnotationVersion;
import ai.classifai.util.ParamConfig;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/*
* Objective of this class:
* 1) retrieve information of the annotation from database
* 2) sort label according to class
* 3) send label and unlabeled information
* */

@Slf4j
public class LabelListHandler {

    // Handling the number of labeled and unlabeled image
    public static ArrayList<ArrayList<JsonArray>> getImageLabeledStatus(Map<String, Annotation> uuidAnnotationDict) {

        Set<String> imageUUID = uuidAnnotationDict.keySet(); // key for each project
        List<Annotation> annotationList = getAnnotationList(imageUUID, uuidAnnotationDict);// a list of Image metadata
        ArrayList <JsonArray> labeledImageList= new ArrayList<>();
        ArrayList <JsonArray> unlabeledImageList = new ArrayList<>();

        // Every annotation represent an image
        for (Annotation annotation : annotationList) {

            LinkedHashMap<String, JsonObject> annotationDataMap = getAnnotationData(annotation.getAnnotationDictDbFormat()); // version uuid, annotation data
            JsonArray labelPointData = getAnnotationStatus(String.valueOf(annotationDataMap.values()));

            if (getAnnotatedImage(labelPointData) != null) {
                labeledImageList.add(labelPointData);
            } else {
                unlabeledImageList.add(labelPointData);
            }

        }

        ArrayList<ArrayList<JsonArray>> totalImage = new ArrayList<>();
        totalImage.add(labeledImageList);
        totalImage.add(unlabeledImageList);

        return totalImage;

    }


    private static LinkedHashMap<String,JsonObject > getAnnotationData(String annotationDict) {

        LinkedHashMap<String, JsonObject> annotationDataMap = new LinkedHashMap<>();
        String s = StringUtils.removeStart(StringUtils.removeEnd(annotationDict, "]"), "[");

        JsonObject annotationDictJsonObject = new JsonObject(s);
        String versionUuid = annotationDictJsonObject.getString(ParamConfig.getVersionUuidParam());
        JsonObject annotationData= annotationDictJsonObject.getJsonObject(ParamConfig.getAnnotationDataParam());

        annotationDataMap.put(versionUuid, annotationData);

        return annotationDataMap;

    }

    private static JsonArray getAnnotationStatus(String annotationDictValue){

        String s = StringUtils.removeStart(StringUtils.removeEnd(annotationDictValue, "]"), "[");
        JsonObject annotationDataJsonObject = new JsonObject(s); //annotation, img_x, img_y, img_w, img_h

        // To get annotation parameters
        return annotationDataJsonObject.getJsonArray(ParamConfig.getAnnotationParam());// x1, y1, x2, y2, color, distToImg, label ,id

    }

    private static Number getAnnotatedImage(JsonArray labelPointData) {

        if(!labelPointData.isEmpty()) {
            return labelPointData.size();
        } else {
            return null;
        }

    }

    public static String getLabelPerClassPerImage(Map<String, Annotation> uuidAnnotationDict) {

        Set<String> imageUUID = uuidAnnotationDict.keySet(); // key for each project
        List<Annotation> annotationList = getAnnotationList(imageUUID, uuidAnnotationDict); // a list of Image metadata
        Map<String, Integer> labelByClass;

        JsonArray jsonArray = new JsonArray();
        JsonObject jsonObject = new JsonObject();

        for (Annotation annotation : annotationList) {

            LinkedHashMap<String, JsonObject> annotationDataMap = getAnnotationData(annotation.getAnnotationDictDbFormat());
            JsonArray labelPointData = getAnnotationStatus(String.valueOf(annotationDataMap.values()));
            labelByClass = getLabelByClass(labelPointData);

            jsonObject.put(annotation.getUuid(), labelByClass);
            jsonArray.add(jsonObject);
        }

        return jsonArray.encode();

    }

    private static Map<String, Integer> getLabelByClass(JsonArray labelPointData){

        Map<String, Integer> labelByClass = new HashMap<>();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < labelPointData.size(); i++) {
            JsonObject jsonArray = labelPointData.getJsonObject(i);
            String label = jsonArray.getString("label");
            labels.add(label);
        }

        for (String label : labels) {

            Integer count = labelByClass.get(label);

            if (count == null) {
                count = 0;
            }

            labelByClass.put(label, count + 1);
        }

        return labelByClass;
    }

    private static List<Annotation> getAnnotationList(Set<String> imageUUID, Map<String, Annotation> uuidAnnotationDict){

        List<Annotation> annotationList = new ArrayList<>();

        for (String s : imageUUID) {
            annotationList.add(uuidAnnotationDict.get(s));
        }

        return annotationList;
    }


}
