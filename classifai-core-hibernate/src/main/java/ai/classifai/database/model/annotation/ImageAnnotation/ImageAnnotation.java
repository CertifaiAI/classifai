package ai.classifai.database.model.annotation.ImageAnnotation;

import ai.classifai.database.model.Label;
import ai.classifai.database.model.annotation.Annotation;
import ai.classifai.database.model.versiondata.ImageDataVersion;

import javax.persistence.*;

@Entity(name = "IMAGE_ANNOTATION")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ImageAnnotation extends Annotation
{
    private String color;

    private String label;

    private int lineWidth;
}
