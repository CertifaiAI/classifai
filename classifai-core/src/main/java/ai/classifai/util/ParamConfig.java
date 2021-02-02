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
import ai.classifai.util.type.AnnotationType;
import ai.classifai.util.type.OS;
import lombok.Getter;
import lombok.Setter;

import java.io.File;

/**
 * Parameter for server in general
 *
 * @author codenamewei
 */

public class ParamConfig
{

    @Getter @Setter private static Integer hostingPort;

    private static final String FILE_SEPARATOR;
    private static final OSManager OS_MANAGER;

    static
    {
        OS_MANAGER = new OSManager();

        hostingPort = 9999;

        if (OS_MANAGER.getCurrentOS().equals(OS.WINDOWS))
        {
            FILE_SEPARATOR = "\\\\";
        }
        else
        {
            FILE_SEPARATOR = File.separator;
        }
    }

    private static final File ROOT_SEARCH_PATH = new File(System.getProperty("user.home"));
    private static final String LOG_FILE_PATH = DbConfig.getRootPath() + File.separator + "logs" + File.separator + "classifai.log";

    private static boolean isDockerEnv = false;

    private static final String PROJECT_NAME_PARAM = "project_name";
    private static final String PROJECT_ID_PARAM = "project_id";
    private static final String FILE_SYS_PARAM = "file_sys";

    private static final String UUID_GENERATOR_PARAM = "uuid_generator_seed";
    private static final String ANNOTATE_TYPE_PARAM = "annotation_type";

    private static final String TOTAL_UUID_PARAM = "total_uuid";
    private static final String UUID_LIST_PARAM = "uuid_list";
    private static final String LABEL_LIST_PARAM = "label_list";

    private static final String UUID_PARAM = "uuid";
    private static final String IMAGE_PATH_PARAM = "img_path";

    private static final String EMPTY_ARRAY = "[]";

    private static final String IMAGE_THUMBNAIL_PARAM = "img_thumbnail";
    private static final String IMAGE_SRC_PARAM = "img_src";

    private static final String BOUNDING_BOX_PARAM = "bnd_box";
    private static final String SEGMENTATION_PARAM = "polygons";
    //Common name when performing data migration
    private static final String PROJECT_CONTENT_PARAM = "content";

    private static final String IMAGEX_PARAM = "img_x";
    private static final String IMAGEY_PARAM = "img_y";

    private static final String IMAGEW_PARAM = "img_w";
    private static final String IMAGEH_PARAM = "img_h";
    private static final String IMAGEORIW_PARAM = "img_ori_w";
    private static final String IMAGEORIH_PARAM = "img_ori_h";

    private static final String IMAGE_DEPTH = "img_depth";

    private static final String BASE64_PARAM = "base64";
    private static final String FILE_SIZE_PARAM = "file_size";

    private static final String FILE_PARAM = "file";
    private static final String FOLDER_PARAM = "folder";

    private static final String ACTION_KEYWORD = "action";
    private static final String CONTENT = "content";
    private static final String PROGRESS_METADATA = "progress";


    //v2
    private static final String IS_NEW = "is_new";
    private static final String IS_STARRED = "is_starred";
    private static final String IS_LOADED = "is_loaded";
    private static final String CREATED_DATE = "created_date";

    private static final String STATUS_PARAM = "status";

    public static OSManager getOsManager() {return OS_MANAGER;}

    public static String getFileSeparator() {return FILE_SEPARATOR;}

    public static File getFileSysRootSearchPath() { return ROOT_SEARCH_PATH; }
    public static String getLogFilePath() { return LOG_FILE_PATH; }

    public static void setIsDockerEnv(boolean state) { isDockerEnv = state; }
    public static boolean isDockerEnv() { return isDockerEnv; }

    public static String getProjectNameParam() { return PROJECT_NAME_PARAM; }
    public static String getProjectIDParam() { return PROJECT_ID_PARAM; }
    public static String getFileSysParam() { return FILE_SYS_PARAM; }

    public static String getUuidGeneratorParam() { return UUID_GENERATOR_PARAM; }
    public static String getAnnotateTypeParam() { return ANNOTATE_TYPE_PARAM; }

    public static String getTotalUUIDParam() { return TOTAL_UUID_PARAM; }
    public static String getUUIDListParam() { return UUID_LIST_PARAM; }
    public static String getLabelListParam() { return LABEL_LIST_PARAM;}

    public static String getUUIDParam() { return UUID_PARAM; }
    public static String getImagePathParam() { return IMAGE_PATH_PARAM; }

    public static String getEmptyArray() { return EMPTY_ARRAY; }

    public static String getImageThumbnailParam() { return IMAGE_THUMBNAIL_PARAM; }
    public static String getImageSourceParam() { return IMAGE_SRC_PARAM; }

    public static String getBoundingBoxParam() { return BOUNDING_BOX_PARAM; }
    public static String getSegmentationParam() { return SEGMENTATION_PARAM; }
    public static String getProjectContentParam() { return PROJECT_CONTENT_PARAM; }

    public static String getImageXParam() { return IMAGEX_PARAM; }
    public static String getImageYParam() { return IMAGEY_PARAM; }

    public static String getImageWParam() { return IMAGEW_PARAM; }
    public static String getImageHParam() { return IMAGEH_PARAM; }

    public static String getImageORIWParam() { return IMAGEORIW_PARAM; }
    public static String getImageORIHParam() { return IMAGEORIH_PARAM; }

    public static String getImageDepth() { return IMAGE_DEPTH; }

    public static String getBase64Param() { return BASE64_PARAM; }
    public static String getFileSizeParam() { return FILE_SIZE_PARAM; }

    public static String getFileParam() { return FILE_PARAM; }
    public static String getFolderParam() { return FOLDER_PARAM; }

    public static String getActionKeyword() { return ACTION_KEYWORD; }
    public static String getContent() { return CONTENT; }
    public static String getProgressMetadata() { return PROGRESS_METADATA; }

    public static String getAnnotationParam(AnnotationType type)
    {
        if (type.equals(AnnotationType.BOUNDINGBOX))
        {
            return ParamConfig.getBoundingBoxParam();
        }
        else if (type.equals(AnnotationType.SEGMENTATION))
        {
            return ParamConfig.getSegmentationParam();
        }
        //ADD WHEN HAVE NEW ANNOTATION TYPE

        return null;
    }

    //v2
    public static String getIsStarredParam() { return IS_STARRED; }
    public static String getIsLoadedParam() { return IS_LOADED; }
    public static String getIsNewParam() { return IS_NEW; }

    public static String getCreatedDateParam() { return CREATED_DATE; }

    public static String getStatusParam() { return STATUS_PARAM; }
}