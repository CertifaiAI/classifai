package ai.classifai.backend.repository;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCPool;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class JdbcHolder {
    @Getter
    private JDBCPool projectPool;

    @Getter
    private JDBCPool annotationPool;

    public JdbcHolder(Vertx vertx) {
        projectPool = createPoolForProjectTable(vertx);
        annotationPool = createPoolForAnnotationTable(vertx);
        createJdbcPool();
    }

    private JDBCPool createPoolForProjectTable(Vertx vertx) {
        String targetFolder = System.getProperty("user.home") + File.separator + "Project";
        createDatabase(targetFolder);
        return JDBCPool.pool(vertx, new JsonObject()
                .put("url", "jdbc:h2:file:" + targetFolder + File.separator +  "project")
                .put("driver_class", "org.h2.Driver")
                .put("user", "admin")
                .put("password", "")
                .put("max_pool_size", 30));
    }

    private JDBCPool createPoolForAnnotationTable(Vertx vertx) {
        String targetFolder = System.getProperty("user.home") + File.separator + "Annotation";
        createDatabase(targetFolder);
        return JDBCPool.pool(vertx, new JsonObject()
                .put("url", "jdbc:h2:file:" + targetFolder + File.separator + "annotation")
                .put("driver_class", "org.h2.Driver")
                .put("user", "admin")
                .put("password", "")
                .put("max_pool_size", 30));
    }

    private void createDatabase(String targetFolder) {
        File databaseFolder = new File(targetFolder);
        if(!databaseFolder.exists()) {
            if (databaseFolder.mkdirs()) {
                log.info("Database folder created at " + targetFolder);
            } else {
                log.info("database is exist");
            }
        }
    }

    public void createJdbcPool() {
        createTable(projectPool, SqlQueries.getCreateProjectTable());
    }

    private void createTable(JDBCPool pool, String query) {
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

    public Future<Void> stop() {
        final List<Future> futures = new ArrayList<>();
        futures.add(projectPool.close());
        futures.add(annotationPool.close());

        return CompositeFuture.all(futures).mapEmpty();
    }
}
