package ai.classifai.view.annotation;

import ai.classifai.util.type.AnnotationType;

public interface AnnotationView
{
    static AnnotationView getAnnotationView(Integer type)
    {
        AnnotationType annotationType = AnnotationType.fromInt(type);

        return switch(annotationType)
        {
            case BOUNDINGBOX -> new BoundingBoxAnnotationView();
            case SEGMENTATION -> new PolygonAnnotationView();
        };
    }
}
