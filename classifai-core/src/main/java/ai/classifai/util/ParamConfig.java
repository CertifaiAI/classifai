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
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * Parameter for server in general
 *
 * @author codenamewei
 */
@Slf4j
public class ParamConfig
{
    private ParamConfig()
    {
        log.debug("Parameters LUT");
    }

    @Setter @Getter private static Integer hostingPort = 9999;
    @Setter @Getter private static boolean isDockerEnv = false;

    @Getter private static final OSManager osManager = new OSManager();

    @Getter private static final String fileSeparator = osManager.getCurrentOS().equals(OS.WINDOWS) ? "\\\\" : File.separator;

    @Getter private static final File rootSearchPath = new File(System.getProperty("user.home"));
    @Getter private static final String logFilePath = DbConfig.getDbRootPath() + File.separator + "logs" + File.separator + "classifai.log";

    @Getter private static final String projectNameParam = "projectName";
    @Getter private static final String projectIdParam = "projectId";

    @Getter private static final String annotationTypeParam = "annotationType";
    @Getter private static final String annotationParam = "annotation";


    @Getter private static final String totalUuidParam = "totalUuid";
    @Getter private static final String uuidListParam = "uuidList";
    @Getter private static final String labelListParam = "labelList";

    @Getter private static final String uuidParam = "uuid";
    @Getter private static final String imgPathParam = "imgPath";

    @Getter private static final String imgSrcParam = "imgSrc";

    @Getter private static final String imgXParam = "imgX";
    @Getter private static final String imgYParam = "imgY";

    @Getter private static final String imgWParam = "imgW";
    @Getter private static final String imgHParam = "imgH";
    @Getter private static final String imgOriWParam = "imgOriW";
    @Getter private static final String imgOriHParam = "imgOriH";

    @Getter private static final String imgDepth = "imgDepth";
    @Getter private static final String fileSizeParam = "fileSize";

    @Getter private static final String fileParam = "file";
    @Getter private static final String folderParam = "folder";

    @Getter private static final String actionKeyword = "action";
    @Getter private static final String content = "content";
    @Getter private static final String progressMetadata = "progress";

    //v2
    @Getter private static final String isNewParam = "isNew";
    @Getter private static final String isStarredParam = "isStarred";
    @Getter private static final String isLoadedParam = "isLoaded";
    @Getter private static final String createdDateParam = "createdData";

    @Getter private static final String statusParam = "status";

    //keyword to retrieve image thumbnail
    @Getter private static final String imgThumbnailParam = "imgThumbnail";
    @Getter private static final String base64Param = "base64";

    //Common name when performing data migration
    @Getter private static final String projectContentParam = "content";

    //router endpoint
    @Getter private static final String fileSysParam = "file_sys";
    @Getter private static final String emptyArray = "[]";
}