package ai.classifai.util.async;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;

public class JsonStoreUtil
{
    /**
     * Method used to store an object which is required in multiple functions in a vertx sequential call
     *
     * @param object object need to be stored
     * @param store json object that is used to store the object. It must be final variable
     * @param key string key to store the object at
     * @return void
     */
    public static Future<Void> storeItem(Object object, JsonObject store, String key)
    {
        Promise<Void> promise = Promise.promise();

        store.put(key, object);

        promise.complete();

        return promise.future();
    }

}
