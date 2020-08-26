/*
 * Copyright (c) 2020 CertifAI
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

package ai.classifai.database.boundingboxdb;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

//FIXME: Comment and interface
public interface BoundingBoxDbServiceable
{

    /*
    GET http://localhost:{port}/retrievedata/:uuid

    Result:
    ImageName varchar(255)
    BoundingBox varchar(2000)
    ImageX integer
    ImageY integer
    ImageW double
    ImageH double
    ImageOriW integer
    ImageOriH integer
    */
    void retrieveData(Message<JsonObject> message);


    /*
    PUT http://localhost:{port}/updatedata
    Body:
    UUID
    BoundingBox varchar(2000)
    ImageX integer
    ImageY integer
    ImageW double
    ImageH double
    ImageOriW integer
    ImageOriH integer
    */
    void updateData(Message<JsonObject> message);
}
