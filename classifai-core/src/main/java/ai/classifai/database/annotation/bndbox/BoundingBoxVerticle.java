 /*
 * Copyright (c) 2020-2021 CertifAI Sdn. Bhd.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.classifai.database.annotation.bndbox;

 import ai.classifai.database.DBUtils;
 import ai.classifai.database.DbConfig;
 import ai.classifai.database.annotation.AnnotationVerticle;
 import ai.classifai.util.ParamConfig;
 import ai.classifai.util.message.ErrorCodes;
 import ai.classifai.util.type.AnnotationHandler;
 import ai.classifai.util.type.AnnotationType;
 import ai.classifai.util.type.database.H2;
 import ai.classifai.util.type.database.RelationalDb;
 import io.vertx.core.Promise;
 import io.vertx.core.Vertx;
 import io.vertx.core.eventbus.Message;
 import io.vertx.core.json.JsonObject;
 import io.vertx.jdbcclient.JDBCPool;
 import lombok.extern.slf4j.Slf4j;

/**
 * Bounding Box Verticle
 *
 * @author codenamewei
 */
@Slf4j
public class BoundingBoxVerticle extends AnnotationVerticle
{
    public void onMessage(Message<JsonObject> message)
    {
        if (!message.headers().contains(ParamConfig.getActionKeyword()))
        {
            log.error("No action header specified for message with headers {} and body {}",
                    message.headers(), message.body().encodePrettily());

            message.fail(ErrorCodes.NO_ACTION_SPECIFIED.ordinal(), "No keyword " + ParamConfig.getActionKeyword() + " specified");
        }
    }

    public static JDBCPool createJDBCPool(Vertx vertx, RelationalDb db)
    {
        return JDBCPool.pool(vertx, new JsonObject()
                .put("url", db.getUrlHeader() + DbConfig.getTableAbsPathDict().get(DbConfig.getBndBoxKey()))
                .put("driver_class", db.getDriver())
                .put("user", db.getUser())
                .put("password", db.getPassword())
                .put("max_pool_size", 30));
    }

    @Override
    public void stop(Promise<Void> promise)
    {
        jdbcPool.close();

        log.info("Bounding Box Verticle stopping...");
    }

    //obtain a JDBC pool connection,
    //Performs a SQL query to create the pages table unless it already existed
    @Override
    public void start(Promise<Void> promise) throws Exception
    {
        H2 h2 = DbConfig.getH2();

        jdbcPool = createJDBCPool(vertx, h2);

        jdbcPool.getConnection(ar -> {

            if (ar.failed())
            {
                log.error("Could not open a database connection for Bounding Box Verticle", ar.cause());
                promise.fail(ar.cause());
            }
            else
            {
                jdbcPool.query(BoundingBoxDbQuery.getCreateProject())
                        .execute()
                        .onComplete(DBUtils.handleResponse(
                                result -> {
                                    AnnotationHandler.addJDBCPool(AnnotationType.BOUNDINGBOX, jdbcPool);

                                    //the consumer methods registers an event bus destination handler
                                    vertx.eventBus().consumer(BoundingBoxDbQuery.getQueue(), this::onMessage);
                                    promise.complete();
                                },
                                cause -> {
                                    log.error("BoundingBoxVerticle database preparation error ", cause);
                                    promise.fail(cause);
                                }
                        ));
            }
        });
    }
}
