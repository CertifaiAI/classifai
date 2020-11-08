/*
 * Copyright (c) 2020 CertifAI Sdn. Bhd.
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

import ai.classifai.ui.button.OSManager;
import ai.classifai.util.type.AnnotationType;
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
    private final static OSManager OS_MANAGER;

    static
    {
        OS_MANAGER = new OSManager();

        hostingPort = 9999;
    }

    private static final File ROOT_SEARCH_PATH = new File(System.getProperty("user.home"));
    private static final String LOG_FILE_PATH = System.getProperty("user.home") +  File.separator + "logs" + File.separator + "classifai.log";

    private final static String PROJECT_NAME_PARAM = "project_name";
    private final static String PROJECT_ID_PARAM = "project_id";
    private final static String FILE_SYS_PARAM = "file_sys";

    private final static String UUID_GENERATOR_PARAM = "uuid_generator_seed";
    private final static String ANNOTATE_TYPE_PARAM = "annotation_type";

    private final static String UUID_LIST_PARAM = "uuid_list";
    private final static String LABEL_LIST_PARAM = "label_list";

    private final static String UUID_PARAM = "uuid";
    private final static String IMAGE_PATH_PARAM = "img_path";

    private final static String EMPTY_ARRAY = "[]";

    private final static String IMAGE_THUMBNAIL_PARAM = "img_thumbnail";
    private final static String IMAGE_SRC_PARAM = "img_src";

    private final static String BOUNDING_BOX_PARAM = "bnd_box";
    private final static String SEGMENTATION_PARAM = "polygons";

    private final static String IMAGEX_PARAM = "img_x";
    private final static String IMAGEY_PARAM = "img_y";

    private final static String IMAGEW_PARAM = "img_w";
    private final static String IMAGEH_PARAM = "img_h";
    private final static String IMAGEORIW_PARAM = "img_ori_w";
    private final static String IMAGEORIH_PARAM = "img_ori_h";

    private final static String IMAGE_DEPTH = "img_depth";

    private final static String BASE64_PARAM = "base64";
    private final static String FILE_SIZE_PARAM = "file_size";

    private static final String FILE_PARAM = "file";
    private static final String FOLDER_PARAM = "folder";

    private final static String ACTION_KEYWORD = "action";
    private final static String CONTENT = "content";
    private final static String PROGRESS_METADATA = "progress";

    public static OSManager getOSManager() { return OS_MANAGER; }
    public static File getFileSysRootSearchPath() { return ROOT_SEARCH_PATH; }
    public static String getLogFilePath() { return LOG_FILE_PATH; }

    public static String getProjectNameParam() { return PROJECT_NAME_PARAM; }
    public static String getProjectIDParam() { return PROJECT_ID_PARAM; }
    public static String getFileSysParam() { return FILE_SYS_PARAM; }

    public static String getUuidGeneratorParam() { return UUID_GENERATOR_PARAM; }
    public static String getAnnotateTypeParam() { return ANNOTATE_TYPE_PARAM; }

    public static String getUUIDListParam() { return UUID_LIST_PARAM; }
    public static String getLabelListParam() { return LABEL_LIST_PARAM;}

    public static String getUUIDParam(){ return UUID_PARAM; }
    public static String getImagePathParam(){ return IMAGE_PATH_PARAM; }

    public static String getEmptyArray(){ return EMPTY_ARRAY; }

    public static String getImageThumbnailParam(){ return IMAGE_THUMBNAIL_PARAM; }
    public static String getImageSourceParam(){ return IMAGE_SRC_PARAM; }

    public static String getBoundingBoxParam(){ return BOUNDING_BOX_PARAM; }
    public static String getSegmentationParam(){ return SEGMENTATION_PARAM; }

    public static String getImageXParam() { return IMAGEX_PARAM; }
    public static String getImageYParam() { return IMAGEY_PARAM; }

    public static String getImageWParam() { return IMAGEW_PARAM; }
    public static String getImageHParam() { return IMAGEH_PARAM; }

    public static String getImageORIWParam() { return IMAGEORIW_PARAM; }
    public static String getImageORIHParam() { return IMAGEORIH_PARAM; }

    public static String getImageDepth() { return IMAGE_DEPTH; }

    public static String getBase64Param() { return BASE64_PARAM; }
    public static String getFileSizeParam() { return FILE_SIZE_PARAM; }


    public static String getFileParam(){ return FILE_PARAM; }
    public static String getFolderParam(){ return FOLDER_PARAM; }

    public static String getActionKeyword() { return ACTION_KEYWORD; }
    public static String getContent() { return CONTENT; }
    public static String getProgressMetadata() { return PROGRESS_METADATA; }

    public static String getAnnotationParam(AnnotationType type)
    {
        if(type.equals(AnnotationType.BOUNDINGBOX))
        {
            return ParamConfig.getBoundingBoxParam();
        }
        else if(type.equals(AnnotationType.SEGMENTATION))
        {
            return ParamConfig.getSegmentationParam();
        }
        //TODO: Add more annotation type here when added

        return null;
    }
}