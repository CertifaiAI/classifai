/*
 * Copyright (c) 2021 CertifAI Sdn. Bhd.
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
package ai.classifai.database.repository.generic;

import ai.classifai.core.entity.trait.HasId;
import ai.classifai.core.service.generic.AbstractRepository;
import ai.classifai.core.service.generic.Repository;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Abstract class for abstract repository with hibernate implementation
 *
 * @author YinChuangSum
 */
@AllArgsConstructor
public abstract class AbstractHibernateRepository<Entity extends HasId<Id>, DTO, Id, EntityImpl extends Entity> implements AbstractRepository<Entity, DTO, Id>
{
    protected EntityManager em;
    protected final Class<? extends EntityImpl> entityClass;

    public static final String CLASS_CAST_ERROR_MESSAGE = "Entity given is not hibernate entity, wrong pair of repository and entity is used!";

    @Override
    public void delete(@NonNull Entity entity)
    {
        if (! em.contains(entity))
        {
            entity = em.merge(entity);
        }

        em.remove(entity);
    }

    @Override
    public Entity get(@NonNull Id id)
    {
        return em.find(entityClass, id);
    }

    @SuppressWarnings("unchecked")
    protected EntityImpl toEntityImpl(Entity entity)
    {
        try
        {
            return (EntityImpl) entity;
        }
        catch (ClassCastException e)
        {
            throw new IllegalArgumentException(CLASS_CAST_ERROR_MESSAGE);
        }
    }
}
