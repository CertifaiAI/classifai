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
package ai.classifai.util;

import ai.classifai.database.DbConfig;
import ai.classifai.ui.component.OSManager;
import ai.classifai.util.type.OS;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * Parameter for server in general
 *
 * @author codenamewei
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ParamConfig
{
    @Setter @Getter private static Integer hostingPort = 9999;
    @Setter @Getter private static boolean isDockerEnv = false;

    @Getter private static final OSManager osManager = new OSManager();

    @Getter private static final String fileSeparator = osManager.getCurrentOS().equals(OS.WINDOWS) ? "\\\\" : File.separator;

    @Getter private static final File rootSearchPath = new File(System.getProperty("user.home"));
    @Getter private static final String logFilePath = DbConfig.getDbRootPath() + File.separator + "logs" + File.separator + "classifai.log";

    @Getter private static final String dateTimeFormat = "yyyy/MM/dd hh:mm:ss a";

    @Getter private static final String projectNameParam = "project_name";
    @Getter private static final String newProjectNameParam = "new_project_name";
    @Getter private static final String projectIdParam = "project_id";
    @Getter private static final String projectPathParam = "project_path";

    @Getter private static final String annotationTypeParam = "annotation_type";
    @Getter private static final String annotationParam = "annotation";

    @Getter private static final String boundingBoxParam = "bnd_box";
    @Getter private static final String segmentationParam = "polygons";

    @Getter private static final String totalUuidParam = "total_uuid";
    @Getter private static final String uuidListParam = "uuid_list";
    @Getter private static final String labelListParam = "label_list";

    @Getter private static final String uuidParam = "uuid";
    @Getter private static final String imgPathParam = "img_path";

    @Getter private static final String imgSrcParam = "img_src";

    @Getter private static final String imgXParam = "img_x";
    @Getter private static final String imgYParam = "img_y";

    @Getter private static final String imgWParam = "img_w";
    @Getter private static final String imgHParam = "img_h";
    @Getter private static final String imgOriWParam = "img_ori_w";
    @Getter private static final String imgOriHParam = "img_ori_h";

    @Getter private static final String imgDepth = "img_depth";

    @Getter private static final String fileParam = "file";
    @Getter private static final String folderParam = "folder";

    @Getter private final static String fileSizeParam = "file_size";

    @Getter private static final String actionKeyword = "action";
    @Getter private static final String content = "content";
    @Getter private static final String progressMetadata = "progress";

    //*******************************V2*******************************

    @Getter private static final String isNewParam = "is_new";
    @Getter private static final String isStarredParam = "is_starred";
    @Getter private static final String isLoadedParam = "is_loaded";
    @Getter private static final String createdDateParam = "created_date";

    @Getter private static final String statusParam = "status";

    @Getter private static final String versionListParam = "version_list";

    @Getter private static final String uuidVersionListParam = "uuid_version_list";
    @Getter private static final String labelVersionListParam = "label_version_list";

    //keyword to retrieve image thumbnail
    @Getter private static final String imgThumbnailParam = "img_thumbnail";
    @Getter private static final String base64Param = "base64";

    //Common name when performing data migration
    @Getter private static final String projectContentParam = "content";

    //router endpoint
    @Getter private static final String fileSysParam = "file_sys";
    @Getter private static final String emptyArray = "[]";

    //reload project
    @Getter private static final String uuidDeletionListParam = "uuid_delete_list";
    @Getter private static final String uuidAdditionListParam = "uuid_add_list";

    //versioning
    @Getter private static final String currentVersionParam = "current_version";
    @Getter private static final String projectVersionParam = "project_version";

    @Getter private static final String versionUuidParam = "version_uuid";
    @Getter private static final String nextVersionUuidParam = "next_version_uuid";

    @Getter private static final String annotationDataParam = "annotation_data";

    //hash
    @Getter private static final String checkSumParam = "checksum";

    //cloud
    @Getter private static final String isCloudParam = "is_cloud";
    @Getter private static final String projectInfraParam = "project_infra";
}