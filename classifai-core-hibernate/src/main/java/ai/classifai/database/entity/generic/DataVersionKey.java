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
package ai.classifai.database.entity.generic;

import ai.classifai.core.entity.model.generic.DataVersion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Composite key for DataVersion entity with hibernate implementation
 *
 * @author YinChuangSum
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class DataVersionKey implements Serializable, DataVersion.DataVersionId
{
    @Serial
    private static final long serialVersionUID = 2061522709097353044L;

    private UUID dataId;

    private UUID versionId;
}
