package ai.classifai.backend.repository;

import ai.classifai.backend.repository.query.AnnotationQuery;
import ai.classifai.backend.repository.query.PortfolioDbQuery;
import ai.classifai.core.utility.DbConfig;
import ai.classifai.core.utility.type.H2;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCPool;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class JDBCPoolHolder {
    private final Vertx vertx;
    private final H2 db;
    @Getter
    private JDBCPool annotationPool;

    @Getter
    private JDBCPool portfolioPool;

    public JDBCPoolHolder(Vertx vertx, H2 db) {
        this.vertx = vertx;
        this.db = db;
        init();
    }

    public void init() {
        annotationPool = createPoolForTable(vertx, db, DbConfig.getAnnotationKey());
        portfolioPool = createPoolForTable(vertx, db, DbConfig.getPortfolioKey());
        createInitialTable(annotationPool, AnnotationQuery.getCreateImageProject());
        createInitialTable(annotationPool, AnnotationQuery.getCreateAudioProject());
        createInitialTable(annotationPool, AnnotationQuery.getCreateVideoProject());
        createInitialTable(portfolioPool, PortfolioDbQuery.getCreatePortfolioTable());
    }

    public void createInitialTable(JDBCPool pool, String query) {
        pool.getConnection(ar -> {
            if (ar.succeeded()) {
                pool.query(query).execute()
                        .onComplete(value -> {
                            if(value.succeeded()) {
                                log.debug("Success: " + query);
                            } else {
                                log.debug("Fail: " + query);
                            }
                        });
            } else {
                log.error("Could not open database connection", ar.cause());
            }
        });
    }

    private JDBCPool createPoolForTable(Vertx vertx, H2 db, String table){
        return JDBCPool.pool(vertx, new JsonObject()
                .put("url", db.getUrlHeader() + DbConfig.getTableAbsPathDict().get(table))
                .put("driver_class", db.getDriver())
                .put("user", db.getUser())
                .put("password", db.getPassword())
                .put("max_pool_size", 30));
    }

    public Future<Void> stop() {
        final List<Future> futures = new ArrayList<>();
        futures.add(portfolioPool.close());
        futures.add(annotationPool.close());

        return CompositeFuture.all(futures).mapEmpty();
    }
}
