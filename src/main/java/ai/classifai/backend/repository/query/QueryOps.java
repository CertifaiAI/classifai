package ai.classifai.backend.repository.query;

import ai.classifai.core.loader.ProjectLoader;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class QueryOps {
    public Future<RowSet<Row>> runQuery(String query, Tuple params, JDBCPool pool) {
        final Promise<RowSet<Row>> promise = Promise.promise();
        pool.preparedQuery(query)
                .execute(params)
                .onComplete(fetch -> {
                    if(fetch.succeeded()) {
                        promise.complete(fetch.result());
                    } else {
                        promise.fail(fetch.cause());
                    }
                });
        return promise.future();
    }

    public Future<RowSet<Row>> runQuery(String query, JDBCPool pool) {
        final Promise<RowSet<Row>> promise = Promise.promise();
        pool.preparedQuery(query)
                .execute()
                .onComplete(fetch -> {
                    if(fetch.succeeded()) {
                        promise.complete(fetch.result());
                    } else {
                        promise.fail(fetch.cause());
                    }
                });
        return promise.future();
    }

    public void runQuery(ProjectLoader loader, String query, Tuple params, JDBCPool pool,Handler<AsyncResult<RowSet<Row>>> handler){
        pool.preparedQuery(query)
                .execute(params)
                .onComplete(handler);
    }

}
