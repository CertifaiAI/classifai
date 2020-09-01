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

package ai.certifai.data.type.image;

import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Support images & documents
 */
@NoArgsConstructor
public class ImageFileType {

    private static final Integer MAX_WIDTH = 5000;
    private static final Integer MAX_HEIGHT = 5000;

    private static final Integer FIXED_THUMBNAIL_WIDTH = 100;
    private static final Integer FIXED_THUMBNAIL_HEIGHT = 100;


    private static final Map base64header;

    private static final String[] ALLOWED_FILE_TYPES = new String[]{"jpg", "png", "jpeg", "pdf", "bmp", "JPG", "PNG", "JPEG", "tif"};

    static
    {
        base64header = new HashMap();
        base64header.put("jpg", "data:image/jpeg;base64,");
        base64header.put("JPG", "data:image/jpeg;base64,");
        base64header.put("jpeg", "data:image/png;base64,");
        base64header.put("JPEG", "data:image/jpeg;base64,");
        base64header.put("png", "data:image/jpeg;base64,");
        base64header.put("PNG", "data:image/png;base64,");
        base64header.put("bmp", "data:image/bmp;base64,");
        base64header.put("tif", "data:image/png;base64,");
    }

    public static String[] getImageFileTypes()
    {
        return ALLOWED_FILE_TYPES;
    }

    public static Map getBase64header()
    {
        return base64header;
    }

    public static Integer getMaxWidth()
    {
        return MAX_WIDTH;
    }

    public static Integer getMaxHeight()
    {
        return MAX_HEIGHT;
    }

    public static Integer getFixedThumbnailWidth()
    {
        return FIXED_THUMBNAIL_WIDTH;
    }

    public static Integer getFixedThumbnailHeight()
    {
        return FIXED_THUMBNAIL_HEIGHT;
    }
}
