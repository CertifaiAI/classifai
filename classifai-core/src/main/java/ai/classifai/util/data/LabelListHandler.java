package ai.classifai.util.data;

import ai.classifai.database.versioning.Annotation;
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
        List<Annotation> annotationList = new ArrayList<>(); // a list of Image metadata
        ArrayList <JsonArray> labeledImageList= new ArrayList<>();
        ArrayList <JsonArray> unlabeledImageList = new ArrayList<>();

        // Getting image metadata : uuid, projectId, imgPath, annotationDict, imgDepth, imgOriW, imgOriH, fileSize
        for (String s : imageUUID) {
            annotationList.add(uuidAnnotationDict.get(s));
        }

        // Every annotation represent an image
        for (Annotation annotation : annotationList) {

            LinkedHashMap<String, JsonObject> annotationDict = getAnnotationDict(annotation.getAnnotationDictDbFormat());
            JsonArray labelPointData = getAnnotationStatus(String.valueOf(annotationDict.values()));

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


    public static LinkedHashMap<String,JsonObject > getAnnotationDict(String annotationDict) {

        String s = StringUtils.removeStart(StringUtils.removeEnd(annotationDict, "]"), "[");

        JsonObject annotationDictJsonObject = new JsonObject(s);
        String versionUuid = annotationDictJsonObject.getString(ParamConfig.getVersionUuidParam());
        JsonObject annotationData= annotationDictJsonObject.getJsonObject(ParamConfig.getAnnotationDataParam());

        LinkedHashMap<String, JsonObject> annotationDataMap = new LinkedHashMap<>();

        annotationDataMap.put(versionUuid, annotationData);

        return annotationDataMap;

    }

    public static JsonArray getAnnotationStatus(String annotationDictValue){

        String s = StringUtils.removeStart(StringUtils.removeEnd(annotationDictValue, "]"), "[");
        JsonObject annotationDataJsonObject = new JsonObject(s); //annotation, img_x, img_y, img_w, img_h

        // To get annotation data
        return annotationDataJsonObject.getJsonArray(ParamConfig.getAnnotationParam());// x1, y1, x2, y2, color, distToImg, label ,id

    }

    public static Number getAnnotatedImage(JsonArray labelPointData) {

        if(!labelPointData.isEmpty()) {
            return labelPointData.size();
        } else {
            return null;
        }

    }

    public static String getImageLabel(Map<String, Annotation> uuidAnnotationDict) {

        Set<String> imageUUID = uuidAnnotationDict.keySet(); // key for each project
        List<Annotation> annotationList = new ArrayList<>(); // a list of Image metadata
        Map<String, Integer> labelByClass = new HashMap<>();
        ArrayList<String> labels = new ArrayList<>();

        for (String s : imageUUID) {
            annotationList.add(uuidAnnotationDict.get(s));
        }

        JsonObject jsonObject = new JsonObject();
        for (Annotation annotation : annotationList) {

            LinkedHashMap<String, JsonObject> annotationDict = getAnnotationDict(annotation.getAnnotationDictDbFormat());
            JsonArray labelPointData = getAnnotationStatus(String.valueOf(annotationDict.values()));

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
                if (label == null) {
                    label = "null";
                }
                labelByClass.put(label, count + 1);
            }

            String versionUUID = getVerssionUUID(annotationDict);
            JsonArray jsonArray = new JsonArray(Collections.singletonList(labelByClass));
            jsonObject.put(versionUUID, jsonArray);
        }

        return Objects.requireNonNull(jsonObject).toString();

    }

    public static String getVerssionUUID(LinkedHashMap<String, JsonObject> annotationDict) {

        String s = StringUtils.removeStart(StringUtils.removeEnd(String.valueOf(annotationDict), "]"), "[");
        JsonObject annotationDataJsonObject = new JsonObject(s); //annotation, img_x, img_y, img_w, img_h

        // To get annotation data
        return annotationDataJsonObject.getString(ParamConfig.getVersionUuidParam());

    }


}
