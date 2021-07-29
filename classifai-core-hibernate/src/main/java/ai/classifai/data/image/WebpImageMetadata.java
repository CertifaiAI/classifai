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
package ai.classifai.data.image;

import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.webp.WebpDirectory;

import static com.drew.metadata.webp.WebpDirectory.TAG_IS_ANIMATION;

/**
 * Provides metadata of webp images
 *
 * @author ken479
 */
public class WebpImageMetadata extends ImageMetadata {

    protected WebpImageMetadata(Metadata metadata)
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
        String colorSpaceType = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class).getTagName(ExifDirectoryBase.TAG_COLOR_SPACE);
        if (!colorSpaceType.isEmpty()) {
            return 3;
        } else {
            return 1;
        }
    }

    @Override
    public int getWidth() {
        int orientation = getOrientation();

        if (orientation == 8 || orientation == 6) {
            return getRawHeight();
        }

        return getRawWidth();
    }

    @Override
    public int getHeight() {
        int orientation = getOrientation();

        if (orientation == 8 || orientation == 6) {
            return getRawWidth();
        }

        return getRawHeight();
    }

    @Override
    public boolean isAnimation() {
        try {
            return metadata.getFirstDirectoryOfType(WebpDirectory.class).getBoolean(TAG_IS_ANIMATION);
        } catch (Exception e) {
            return false;
        }
    }
}
