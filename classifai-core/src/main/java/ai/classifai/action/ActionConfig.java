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

import java.util.Arrays;
import java.util.List;

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

    @Getter private static final String tool_name = "classifai";

    @Getter private static final String tool_version = ActionConfig.class.getPackage().getImplementationVersion();

    @Getter private static final List<String> keys = Arrays.asList(
            "tool", "tool_version", "updated_date", "project_id", "project_name", "annotation_type", "is_new",
            "is_starred", "current_version", "project_version", "uuid_version_list", "label_version_list", "content");

    //output file path
    @Getter private static final String projectConfigPathParam = "project_config_path";
}