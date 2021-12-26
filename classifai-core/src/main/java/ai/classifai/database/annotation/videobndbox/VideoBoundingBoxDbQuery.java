package ai.classifai.database.annotation.videobndbox;

import ai.classifai.database.annotation.AnnotationQuery;
import lombok.Getter;

public class VideoBoundingBoxDbQuery extends AnnotationQuery{
    @Getter private static final String queue = "boundingbox.queue";

}
