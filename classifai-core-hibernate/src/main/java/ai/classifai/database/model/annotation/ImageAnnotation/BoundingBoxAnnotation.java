package ai.classifai.database.model.annotation.ImageAnnotation;

import ai.classifai.database.model.Label;

import javax.persistence.*;

@Entity
public class BoundingBoxAnnotation extends ImageAnnotation
{
    private float x1;
    private float x2;
    private float y1;
    private float y2;
    private float dist2ImgX;
    private float dist2ImgY;

}
