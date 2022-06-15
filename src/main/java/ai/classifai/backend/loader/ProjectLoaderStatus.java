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
package ai.classifai.backend.loader;

import lombok.extern.slf4j.Slf4j;

/***
 * Loader status for project loading
 *
 * @author codenamewei
 */
@Slf4j
public enum ProjectLoaderStatus
{
    ERROR,
    LOADING,
    LOADED, //projectloader will have this status once create new project
    DID_NOT_INITIATED, //default value when ProjectLoader created from database
}
