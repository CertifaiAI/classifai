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
package ai.classifai.action;

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
    public enum ExportType
    {
        INVALID_CONFIG,
        CONFIG_ONLY,
        CONFIG_WITH_DATA
    }

    public static final String TOOL_PARAM = "tool";
    public static final String TOOL_VERSION_PARAM = "tool_version";
    public static final String UPDATE_DATE_PARAM = "updated_date";

    public static final String EXPORT_TYPE_PARAM = "export_type";
    public static final String EXPORT_STATUS_PARAM = "export_status";
    public static final String EXPORT_STATUS_MESSAGE_PARAM = "export_status_message";

    public static final String TOOL_NAME = "classifai";

    public static final String TOOL_VERSION = ActionConfig.class.getPackage().getImplementationVersion();

    //output file path
    public static final String PROJECT_CONFIG_PATH_PARAM = "project_config_path";

    // User json file absolute path
    @Getter @Setter private static String jsonFilePath;
}