package ai.classifai.repository.annotation;

import com.fasterxml.jackson.databind.util.JSONPObject;

public class TabularAnnotation {
    JSONPObject tabularAnnotationProperties;
    String uuid;
    int fileSize;

    TabularAnnotation(String uuid, Integer fileSize) {
        this.uuid = uuid;
        this.fileSize = fileSize;
    }

}
