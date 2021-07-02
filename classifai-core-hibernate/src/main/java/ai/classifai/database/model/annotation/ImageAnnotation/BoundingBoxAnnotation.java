package ai.classifai.database.model.annotation.ImageAnnotation;

import ai.classifai.database.model.Label;

import javax.persistence.*;

@Entity(name = "BOUNDING_BOX_ANNOTATION")
public class BoundingBoxAnnotation extends ImageAnnotation
{
    private float x1;
    private float x2;
    private float y1;
    private float y2;
    private float dist2ImgX;
    private float dist2ImgY;

    public BoundingBoxAnnotation(Long annotationId, String color, String label, int lineWidth, float x1, float x2,
                                 float y1, float y2, float dist2ImgX, float dist2ImgY)
    {
        super(annotationId, color, label, lineWidth);
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        this.dist2ImgX = dist2ImgX;
        this.dist2ImgY = dist2ImgY;
    }

    public BoundingBoxAnnotation() {}
}
