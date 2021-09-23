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

package ai.classifai.data.type.image;

import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.webp.WebpDirectory;
import lombok.extern.slf4j.Slf4j;

import static com.drew.metadata.webp.WebpDirectory.TAG_IS_ANIMATION;

/**
 * Provides metadata of webp images
 *
 * @author ken479
 */
@Slf4j
public class WebpImageData extends ImageData{
    private final int undefinedColorSpace = 0;

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
        // Webp does not have EXIF COLOR SPACE TAG
        log.debug("Color space of Webp image is not detected");
        return undefinedColorSpace;
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
