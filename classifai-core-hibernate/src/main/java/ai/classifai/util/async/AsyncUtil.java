package ai.classifai.util.async;

import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.List;

public class AsyncUtil {
    public static <T> Future<Void> storeList(List<T> itemList, List<T> store) {
        Promise<Void> promise = Promise.promise();

        store.addAll(itemList);

        promise.complete();

        return promise.future();
    }

}
