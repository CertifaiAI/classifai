package ai.classifai.database.model.annotation.ImageAnnotation;

import ai.classifai.database.model.Label;
import ai.classifai.database.model.annotation.Annotation;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public abstract class ImageAnnotation extends Annotation
{
    private String color;

    private String label;

    private int lineWidth;
}
