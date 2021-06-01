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
package ai.classifai.selector.filesystem;

/**
 * State of windows to select file/folder
 *
 * @author codenamewei
 */
public enum FileSystemStatus
{
    WINDOW_OPEN,
    WINDOW_CLOSE_NO_ACTION,
    WINDOW_CLOSE_ITERATING_FOLDER,
    WINDOW_CLOSE_DATABASE_UPDATING,
    WINDOW_CLOSE_DATABASE_UPDATED,
    WINDOW_CLOSE_FOLDER_SELECTED,
    DID_NOT_INITIATED
}
