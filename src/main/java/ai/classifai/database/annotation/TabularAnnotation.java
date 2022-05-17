package ai.classifai.database.annotation;

import com.fasterxml.jackson.databind.util.JSONPObject;

public class TabularAnnotation {
    JSONPObject tabularAnnotationProperties;
    String uuid;
    Integer fileSize;

    TabularAnnotation(String uuid, Integer fileSize) {
        this.uuid = uuid;
        this.fileSize = fileSize;
    }

}
