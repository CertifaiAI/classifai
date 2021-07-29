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
package ai.classifai.core.entity.model.generic;

import ai.classifai.core.entity.dto.generic.PointDTO;
import ai.classifai.core.entity.trait.HasDTO;
import ai.classifai.core.entity.trait.HasId;

import java.util.UUID;

/**
 * Point entity interface
 *
 * @author YinChuangSum
 */
public interface Point extends HasDTO<PointDTO>, HasId<UUID>
{
    Float getX();

    Float getY();

    Float getDist2ImgX();

    Float getDist2ImgY();

    Integer getPosition();
}
