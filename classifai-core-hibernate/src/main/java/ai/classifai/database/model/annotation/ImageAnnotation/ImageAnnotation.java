package ai.classifai.database.model.annotation.ImageAnnotation;

import ai.classifai.database.model.annotation.Annotation;

import javax.persistence.*;

@Entity(name = "IMAGE_ANNOTATION")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ImageAnnotation extends Annotation
{
    private String color;

    private String label;

    private int lineWidth;

    public ImageAnnotation(Long annotationId, String color, String label, int lineWidth)
    {
        super(annotationId);
        this.color = color;
        this.label = label;
        this.lineWidth = lineWidth;
    }

    public ImageAnnotation() {}
}
