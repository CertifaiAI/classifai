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
package ai.classifai.database;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * verticle that handles database
 *
 * @author codenamewei
 */
@Slf4j
public class DatabaseVerticle extends AbstractVerticle implements VerticleServiceable
{
    private EntityManagerFactory entityManagerFactory;

    @Override
    public void onMessage(Message<JsonObject> message)
    {

    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception
    {
        vertx.executeBlocking(this::connectDatabase)
                .onSuccess(r -> successHandler(startPromise))
                .onFailure(r -> failureHandler(startPromise, r));
    }

    private void successHandler(Promise<Void> promise)
    {
        //FIXME: remove hardcoded value
        vertx.eventBus().consumer("database", this::onMessage);
        promise.complete();
    }

    private void failureHandler(Promise<Void> promise, Throwable e)
    {
        log.error("Portfolio database preparation error", e.getCause());
        promise.fail(e.getCause());
    }

    private Future<Void> connectDatabase(Promise<Void> promise)
    {
        try
        {
            entityManagerFactory = Persistence.createEntityManagerFactory("ai.classifai.database.jpa");
            promise.complete();
        }
        catch (Exception e)
        {
            promise.fail(e);
        }
        return promise.future();
    }
}
