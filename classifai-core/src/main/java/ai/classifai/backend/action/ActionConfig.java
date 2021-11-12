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
package ai.classifai.backend.action;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * keywords for export use
 *
 * @author codenamewei
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ActionConfig
{
    @Getter private static final String toolParam = "tool";
    @Getter private static final String toolVersionParam = "tool_version";
    @Getter private static final String updatedDateParam = "updated_date";

    @Getter private static final String exportTypeParam = "export_type";
    @Getter private static final String exportStatusParam = "export_status";
    @Getter private static final String exportStatusMessageParam = "export_status_message";

    @Getter private static final String toolName = "classifai";

    @Getter private static final String toolVersion = ActionConfig.class.getPackage().getImplementationVersion();

    //output file path
    @Getter private static final String projectConfigPathParam = "project_config_path";

    // User json file absolute path
    @Getter @Setter private static String jsonFilePath;
}