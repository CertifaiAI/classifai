package ai.classifai.view.data;

import io.vertx.core.json.JsonObject;

/**
 * class for handling image source response
 *
 * @author YinChuangSum
 */
public class ImageSourceView
{
    public static JsonObject generateImageSourceView(String imageBase64)
    {
        return new JsonObject()
                .put("message", 1)
                .put("img_src", imageBase64);
    }
}
