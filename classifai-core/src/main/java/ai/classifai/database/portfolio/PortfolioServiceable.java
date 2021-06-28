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
package ai.classifai.database.portfolio;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

/***
 * common functionalities for Portfolio Vecticle
 *
 * @author codenamewei
 */
public interface PortfolioServiceable
{
    void updateLabelList(Message<JsonObject> message);

    void getAllProjectsForAnnotationType(Message<JsonObject> message);
}
