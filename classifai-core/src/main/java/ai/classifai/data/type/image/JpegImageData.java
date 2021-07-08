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
import com.drew.metadata.jpeg.JpegDirectory;

/**
 * Provides metadata of jpeg images
 *
 * @author YCCertifai
 */
public class JpegImageData extends ImageData
{
    protected JpegImageData(Metadata metadata) {
        super(metadata, JpegDirectory.class);
    }

    private int getRawWidth() {
        try {
            return metadata.getFirstDirectoryOfType(JpegDirectory.class).getInt(JpegDirectory.TAG_IMAGE_WIDTH);
        } catch (MetadataException e) {
            logMetadataError();
            return 0;
        }
    }

    private int getRawHeight() {
        try {
            return metadata.getFirstDirectoryOfType(JpegDirectory.class).getInt(JpegDirectory.TAG_IMAGE_HEIGHT);
        } catch (MetadataException e) {
            logMetadataError();
            return 0;
        }
    }

    /**
     * get Exif orientation from metatdata
     * orientation value: [1: 0 deg, 8: 270 deg, 3: 180 deg, 6: 90 deg]
     * ref: https://www.impulseadventure.com/photo/exif-orientation.html
     *
     * 1 = 0 degree                  (Horizontal, normal)
     * 2 = 0 degree,mirrored         (Mirror horizontally)
     * 3 = 180 degree                (Rotate 180 degree)
     * 4 = 180 degree,mirrored       (Mirror vertically)
     * 5 = 90 degree, mirrored       (Mirror horizontal and rotate 270 degree clockwise)
     * 6 = 90 degree  CW             (Rotate 90 degree clockwise)
     * 7 = 270 degree, mirrored      (Mirror horizontal and rotate 90 degree clockwise)
     * 8 = 270 degree CW             (Rotate 270 degree clockwise)
     *
     * @return orientation
     */

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
    public int getDepth() {
        try {
            return directory.getInt(JpegDirectory.TAG_NUMBER_OF_COMPONENTS);
        } catch (Exception ignored) {
            return 3;
        }
    }

    @Override
    public String getMimeType() {
        return "image/jpg";
    }


}