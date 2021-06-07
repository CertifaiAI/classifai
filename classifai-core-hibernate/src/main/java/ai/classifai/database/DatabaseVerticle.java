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

import ai.classifai.database.model.Project;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.message.ErrorCodes;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Criteria;
import org.hibernate.Transaction;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

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
        // FIXME:
        //  Too many duplicate code
        if (!message.headers().contains(ParamConfig.getActionKeyword()))
        {
            log.error("No action header specified for message with headers {} and body {}",
                    message.headers(), message.body().encodePrettily());

            message.fail(ErrorCodes.NO_ACTION_SPECIFIED.ordinal(), "No keyword " + ParamConfig.getActionKeyword() + " specified");

            return;
        }

        String action = message.headers().get(ParamConfig.getActionKeyword());

        // FIXME:
        //  Hardcoded
        if (action.equals("retrieve-all-project-annotation"))
        {
            this.getAllProjectsForAnnotationType(message);
        }
        else if (action.equals(PortfolioDbQuery.getUpdateLabelList()))
        {
            this.updateLabelList(message);
        }
        else if (action.equals(PortfolioDbQuery.getDeleteProject()))
        {
            this.deleteProject(message);
        }
        //*******************************V2*******************************

        else if (action.equals(PortfolioDbQuery.getRetrieveProjectMetadata()))
        {
            this.getProjectMetadata(message);
        }
        else if (action.equals(DbActionConfig.getGetAllProjectMeta()))
        {
            this.getAllProjectsMetadata(message);
        }
        else if (action.equals(PortfolioDbQuery.getStarProject()))
        {
            this.starProject(message);
        }
        else if(action.equals(PortfolioDbQuery.getReloadProject()))
        {
            this.reloadProject(message);
        }
        else if(action.equals(PortfolioDbQuery.getExportProject()))
        {
            this.exportProject(message);
        }
        else if(action.equals(PortfolioDbQuery.getRenameProject()))
        {
            renameProject(message);
        }
        else
        {
            log.error("Portfolio query error. Action did not have an assigned function for handling.");
        }

    }

    private void getAllProjectsMetadata(Message<JsonObject> message)
    {
        Integer annotationTypeIndex = message.body().getInteger(ParamConfig.getAnnotationTypeParam());

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        vertx.executeBlocking(promise ->{
            try {
                CriteriaBuilder builder = entityManager.getCriteriaBuilder();
                CriteriaQuery<Project> criteria = builder.createQuery(Project.class);
                Root<Project> from = criteria.from(Project.class);
                criteria.select(from);
                criteria.where(builder.equal(from.get(Project.)))
            }
        }).onComplete()
                .onSuccess()
                .onFailure();
        
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
        vertx.eventBus().consumer(DbActionConfig.getQueue(), this::onMessage);
        promise.complete();
    }

    private void failureHandler(Promise<Void> promise, Throwable e)
    {
        log.error("Portfolio database preparation error", e.getCause());
        promise.fail(e.getCause());
    }

    private void connectDatabase(Promise<Void> promise)
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
        promise.future();
    }
}
