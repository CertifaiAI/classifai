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

package ai.classifai.core.data.type.image;

import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.webp.WebpDirectory;

import static com.drew.metadata.exif.ExifDirectoryBase.TAG_COLOR_SPACE;
import static com.drew.metadata.webp.WebpDirectory.TAG_IS_ANIMATION;

/**
 * Provides metadata of webp images
 *
 * @author ken479
 */

public class WebpImageData extends ImageData {

    protected WebpImageData(Metadata metadata)
    {
        super(metadata, WebpDirectory.class, "image/webp");
    }

    @Override
    protected int getRawWidth()
    {
        try {
            return directory.getInt(WebpDirectory.TAG_IMAGE_WIDTH);
        } catch (MetadataException e) {
            logMetadataError();
            return 0;
        }
    }

    @Override
    protected int getRawHeight()
    {
        try {
            return directory.getInt(WebpDirectory.TAG_IMAGE_HEIGHT);
        } catch (MetadataException e) {
            logMetadataError();
            return 0;
        }
    }

    @Override
    public int getDepth()
    {
        try {
            int colorSpace = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class).getInt(TAG_COLOR_SPACE);
            if (colorSpace == 0) return 1;
        } catch (MetadataException e) {
            logMetadataError();
        }
        return 3;
    }

    @Override
    public boolean isAnimation() {
        try {
            return metadata.getFirstDirectoryOfType(WebpDirectory.class).getBoolean(TAG_IS_ANIMATION);
        } catch (MetadataException e) {
            return false;
        }
    }
}
