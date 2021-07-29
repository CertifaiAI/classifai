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

import ai.classifai.core.entity.dto.generic.DataVersionDTO;
import ai.classifai.core.entity.model.generic.DataVersion;
import ai.classifai.core.service.generic.DataVersionRepository;
import ai.classifai.database.entity.generic.DataVersionEntity;

import javax.persistence.EntityManager;

/**
 * Class for Annotation abstract repository with hibernate implementation
 *
 * @author YinChuangSum
 */
public class DataVersionHibernateRepository extends AbstractHibernateRepository<DataVersion, DataVersionDTO, DataVersion.DataVersionId, DataVersionEntity> implements DataVersionRepository
{
    public DataVersionHibernateRepository(EntityManager em, Class<? extends DataVersionEntity> entityClass) {
        super(em, entityClass);
    }

    public DataVersionHibernateRepository(EntityManager em)
    {
        super(em, DataVersionEntity.class);
    }


    @Override
    public DataVersion update(DataVersion dataVersion, DataVersionDTO dto)
    {
        dataVersion.update(dto);
        return null;
    }
}
