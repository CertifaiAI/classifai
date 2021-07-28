package ai.classifai.view.data;

import ai.classifai.util.type.AnnotationType;

public abstract class DataView
{
    public static DataView getDataView(AnnotationType annotationType)
    {
        return switch (annotationType)
        {
            case BOUNDINGBOX, SEGMENTATION -> new ImageDataView();
        };
    }
}
