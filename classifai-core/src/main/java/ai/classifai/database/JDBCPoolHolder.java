package ai.classifai.database;

import ai.classifai.database.annotation.bndbox.BoundingBoxDbQuery;
import ai.classifai.database.annotation.seg.SegDbQuery;
import ai.classifai.database.portfolio.PortfolioDbQuery;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.util.type.AnnotationType;
import ai.classifai.util.type.database.H2;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.SqlClient;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class JDBCPoolHolder {
    //annotationInt, jdbc
    private Map<Integer, JDBCPool> annotationJDBCPool = new HashMap<>();

    @Getter
    private JDBCPool portfolioPool;

    @Getter
    private JDBCPool wasabiPool;

    public void addJDBCPool(@NonNull AnnotationType type, @NonNull JDBCPool jdbcPool)
    {
        annotationJDBCPool.put(type.ordinal(), jdbcPool);
    }

    public JDBCPool getJDBCPool(@NonNull ProjectLoader loader)
    {
        return annotationJDBCPool.get(loader.getAnnotationType());
    }

    public void init(Vertx vertx, H2 db) {

        JDBCPool bndBoxPool = createPoolForTable(vertx, db, DbConfig.getBndBoxKey());
        createInitialTable(bndBoxPool, BoundingBoxDbQuery.getCreateProject());
        addJDBCPool(AnnotationType.BOUNDINGBOX, bndBoxPool);

        JDBCPool segPool = createPoolForTable(vertx, db, DbConfig.getSegKey());
        createInitialTable(segPool, SegDbQuery.getCreateProject());
        addJDBCPool(AnnotationType.SEGMENTATION, segPool);

        portfolioPool = createPoolForTable(vertx, db, DbConfig.getPortfolioKey());
        createInitialTable(portfolioPool, PortfolioDbQuery.getCreatePortfolioTable());

        wasabiPool = createPoolForTable(vertx, db, DbConfig.getWasabiKey());
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

    private static JDBCPool createPoolForTable(Vertx vertx, H2 db, String table){
        return JDBCPool.pool(vertx, new JsonObject()
                .put("url", db.getUrlHeader() + DbConfig.getTableAbsPathDict().get(table))
                .put("driver_class", db.getDriver())
                .put("user", db.getUser())
                .put("password", db.getPassword())
                .put("max_pool_size", 30));
    }

    public Future<Void> stop() {
        final List<Future> futures = new ArrayList<>();
        futures.add(wasabiPool.close());
        futures.add(portfolioPool.close());
        futures.addAll(annotationJDBCPool.values().stream().map(SqlClient::close).collect(Collectors.toList()));

        return CompositeFuture.all(futures).mapEmpty();
    }
}
