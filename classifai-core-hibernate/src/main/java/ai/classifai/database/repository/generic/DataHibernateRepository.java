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

import ai.classifai.core.entity.dto.generic.DataDTO;
import ai.classifai.core.entity.model.generic.Data;
import ai.classifai.core.service.generic.DataRepository;
import ai.classifai.database.entity.generic.DataEntity;

import javax.persistence.EntityManager;
import java.util.UUID;

/**
 * Class for Data abstract repository with hibernate implementation
 *
 * @author YinChuangSum
 */
public class DataHibernateRepository extends AbstractHibernateRepository<Data, DataDTO, UUID, DataEntity>
        implements DataRepository
{
    public DataHibernateRepository(EntityManager em, Class<? extends DataEntity> entityClass) {
        super(em, entityClass);
    }

    public DataHibernateRepository(EntityManager em)
    {
        super(em, DataEntity.class);
    }
}
