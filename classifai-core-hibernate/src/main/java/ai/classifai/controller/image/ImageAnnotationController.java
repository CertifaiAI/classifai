package ai.classifai.controller.image;

import ai.classifai.controller.generic.AnnotationController;
import io.vertx.core.Vertx;

public class ImageAnnotationController extends AnnotationController
{
    public ImageAnnotationController(Vertx vertx)
    {
        super(vertx);
    }
}
