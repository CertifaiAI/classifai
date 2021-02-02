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

import ai.classifai.database.DbConfig;
import ai.classifai.database.annotation.AnnotationVerticle;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.message.ErrorCodes;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Bounding Box Verticle
 *
 * @author codenamewei
 */
@Slf4j
public class BoundingBoxVerticle extends AnnotationVerticle
{
    @Getter private static JDBCClient jdbcClient;

    public void onMessage(Message<JsonObject> message)
    {
        if (!message.headers().contains(ParamConfig.getActionKeyword()))
        {
            log.error("No action header specified for message with headers {} and body {}",
                    message.headers(), message.body().encodePrettily());

            message.fail(ErrorCodes.NO_ACTION_SPECIFIED.ordinal(), "No keyword " + ParamConfig.getActionKeyword() + " specified");
            return;
        }
        String action = message.headers().get(ParamConfig.getActionKeyword());

        if (action.equals(BoundingBoxDbQuery.retrieveData()))
        {
            this.retrieveData(message, jdbcClient, BoundingBoxDbQuery.retrieveData(), AnnotationType.BOUNDINGBOX);
        }
        else if (action.equals(BoundingBoxDbQuery.updateData()))
        {
            this.updateData(message, jdbcClient, BoundingBoxDbQuery.updateData(), AnnotationType.BOUNDINGBOX);
        }
        else if (action.equals(BoundingBoxDbQuery.retrieveDataPath()))
        {
            this.retrieveDataPath(message, jdbcClient, BoundingBoxDbQuery.retrieveDataPath());
        }
        else if (action.equals(BoundingBoxDbQuery.loadValidProjectUUID()))
        {
            this.loadValidProjectUUID(message, jdbcClient, BoundingBoxDbQuery.loadValidProjectUUID());
        }
        else if (action.equals(BoundingBoxDbQuery.deleteProjectUUIDListwithProjectID()))
        {
            this.deleteProjectUUIDListwithProjectID(message, jdbcClient, BoundingBoxDbQuery.deleteProjectUUIDListwithProjectID());
        }
        else if (action.equals(BoundingBoxDbQuery.deleteProjectUUIDList()))
        {
            this.deleteProjectUUIDList(message, jdbcClient, BoundingBoxDbQuery.deleteProjectUUIDList());
        }
        else
        {
            log.error("Bounding Box Verticle query error. Action did not have an assigned function for handling.");
        }
    }

    @Override
    public void stop(Promise<Void> promise)
    {
        log.info("Bounding Box Verticle stopping...");
    }

    //obtain a JDBC client connection,
    //Performs a SQL query to create the pages table unless it already existed
    @Override
    public void start(Promise<Void> promise) throws Exception
    {
        jdbcClient = JDBCClient.create(vertx, new JsonObject()
                .put("url", "jdbc:h2:file:" + DbConfig.getBndboxDbPath())
                .put("driver_class", "org.h2.Driver")
                .put("user", "admin")
                .put("max_pool_size", 30));


        jdbcClient.getConnection(ar -> {

            if (ar.failed())
            {
                log.error("Could not open a database connection for Bounding Box Verticle", ar.cause());
                promise.fail(ar.cause());
            }
            else
            {
                SQLConnection connection = ar.result();
                connection.execute(BoundingBoxDbQuery.createProject(), create -> {
                    connection.close();
                    if (create.failed())
                    {
                        log.error("BoundingBoxVerticle database preparation error", create.cause());
                        promise.fail(create.cause());

                    }
                    else
                    {
                        //the consumer methods registers an event bus destination handler
                        vertx.eventBus().consumer(BoundingBoxDbQuery.getQueue(), this::onMessage);
                        promise.complete();
                    }
                });
            }
        });
    }
}
