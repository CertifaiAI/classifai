package ai.classifai.controller.image;

import io.vertx.core.Vertx;

public class BoundingBoxAnnotationController extends ImageAnnotationController
{
    public BoundingBoxAnnotationController(Vertx vertx)
    {
        super(vertx);
    }
}
