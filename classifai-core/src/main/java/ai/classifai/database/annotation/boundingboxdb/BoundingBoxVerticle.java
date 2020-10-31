/*
 * Copyright (c) 2020 CertifAI Sdn. Bhd.
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
package ai.classifai.database.annotation.boundingboxdb;

import ai.classifai.database.DatabaseConfig;
import ai.classifai.database.annotation.AnnotationVerticle;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.image.AnnotationType;
import ai.classifai.util.message.ErrorCodes;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * Bounding Box Verticle
 *
 * @author codenamewei
 */
@Slf4j
public class BoundingBoxVerticle extends AnnotationVerticle
{
    @Getter private static JDBCClient projectJDBCClient;

    public void onMessage(Message<JsonObject> message) {

        if (!message.headers().contains(ParamConfig.getActionKeyword()))
        {
            log.error("No action header specified for message with headers {} and body {}",
                    message.headers(), message.body().encodePrettily());

            message.fail(ErrorCodes.NO_ACTION_SPECIFIED.ordinal(), "No keyword " + ParamConfig.getActionKeyword() + " specified");
            return;
        }
        String action = message.headers().get(ParamConfig.getActionKeyword());

        if(action.equals(BoundingBoxDbQuery.retrieveData()))
        {
            this.retrieveData(projectJDBCClient, BoundingBoxDbQuery.retrieveData(), AnnotationType.BOUNDINGBOX, message);
        }
        else if(action.equals(BoundingBoxDbQuery.retrieveDataPath()))
        {
            this.retrieveDataPath(projectJDBCClient, BoundingBoxDbQuery.retrieveDataPath(), message);
        }
        else if(action.equals(BoundingBoxDbQuery.updateData()))
        {
            this.updateData(projectJDBCClient, BoundingBoxDbQuery.updateData(), AnnotationType.BOUNDINGBOX, message);
        }
        else if (action.equals(BoundingBoxDbQuery.loadValidProjectUUID()))
        {
            this.loadValidProjectUUID(projectJDBCClient, BoundingBoxDbQuery.retrieveDataPath(), message);
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

        File lockFile = new File(DatabaseConfig.getBBLockFile());

        if(lockFile.exists()) lockFile.delete();
    }

    //obtain a JDBC client connection,
    //Performs a SQL query to create the pages table unless it already existed
    @Override
    public void start(Promise<Void> promise)
    {
        projectJDBCClient = JDBCClient.create(vertx, new JsonObject()
                .put("url", "jdbc:hsqldb:file:" + DatabaseConfig.getBndboxDb())
                .put("driver_class", "org.hsqldb.jdbcDriver")
                .put("max_pool_size", 30));


        projectJDBCClient.getConnection(ar -> {
            if (ar.failed()) {
                log.error("Could not open a database connection for Bounding Box Verticle", ar.cause());
                promise.fail(ar.cause());

            } else {
                SQLConnection connection = ar.result();
                connection.execute(BoundingBoxDbQuery.createProject(), create -> {
                    connection.close();
                    if (create.failed()) {
                        log.error("BoundingBoxVerticle database preparation error", create.cause());
                        promise.fail(create.cause());

                    } else
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
