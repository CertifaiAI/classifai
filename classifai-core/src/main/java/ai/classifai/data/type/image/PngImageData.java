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
package ai.classifai.data.type.image;

import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.png.PngDirectory;


/**
 * Provides metadata of png images
 *
 * @author YCCertifai
 */
public class PngImageData extends ImageData
{
    protected PngImageData(Metadata metadata) {
        super(metadata, PngDirectory.class);
    }

    @Override
    public int getOrientation() {
        try {
            return metadata.getFirstDirectoryOfType(ExifIFD0Directory.class).getInt(ExifIFD0Directory.TAG_ORIENTATION);
        } catch (Exception ignored) {
            // if can't find orientation set as 0 deg
            return 1;
        }
    }

    @Override
    public int getWidth() {
        try {
            return directory.getInt(PngDirectory.TAG_IMAGE_WIDTH);
        } catch (MetadataException e) {
            logMetadataError();
            return 0;
        }
    }

    @Override
    public int getHeight() {
        try {
            return directory.getInt(PngDirectory.TAG_IMAGE_HEIGHT);
        } catch (MetadataException e) {
            logMetadataError();
            return 0;
        }
    }

    /**
     * get color type: [0: grayscale, 2: trueColor(RGB)]
     *
     * @return number of channel 1 or 3
     */
    @Override
    public int getDepth() {
        try {
            int colorType = directory.getInt(PngDirectory.TAG_COLOR_TYPE);

            if (colorType == 0) return 1;
        } catch (Exception ignored) {
        }
        return 3;
    }

    @Override
    public String getMimeType() {
        return "image/png";
    }

}