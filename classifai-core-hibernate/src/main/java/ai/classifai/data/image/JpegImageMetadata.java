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
package ai.classifai.data.image;

import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.jpeg.JpegDirectory;

/**
 * Provides metadata of jpeg images
 *
 * @author YCCertifai
 */
public class JpegImageMetadata extends ImageMetadata
{
    protected JpegImageMetadata(Metadata metadata) {
        super(metadata, JpegDirectory.class, "image/jpg");
    }

    @Override
    protected int getRawWidth() {
        try {
            return directory.getInt(JpegDirectory.TAG_IMAGE_WIDTH);
        } catch (MetadataException e) {
            logMetadataError();
            return 0;
        }
    }

    @Override
    protected int getRawHeight() {
        try {
            return directory.getInt(JpegDirectory.TAG_IMAGE_HEIGHT);
        } catch (MetadataException e) {
            logMetadataError();
            return 0;
        }
    }

    @Override
    public int getDepth() {
        try {
            return directory.getInt(JpegDirectory.TAG_NUMBER_OF_COMPONENTS);
        } catch (Exception ignored) {
            return 3;
        }
    }

    @Override
    public boolean isAnimation() {
        // Jpeg is always static image
        return false;
    }

}