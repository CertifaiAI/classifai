package ai.classifai.controller.image;

import io.vertx.core.Vertx;

/**
 * Class to handle BoundingBoxAnnotation APIs
 *
 * @author YinChuangSum
 */
public class BoundingBoxAnnotationController extends ImageAnnotationController
{
    public BoundingBoxAnnotationController(Vertx vertx)
    {
        super(vertx);
    }
}
