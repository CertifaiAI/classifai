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
import com.drew.metadata.bmp.BmpHeaderDirectory;

/**
 * Provides metadata of bmp images
 *
 * @author YCCertifai
 */
public class BmpImageData extends ImageData
{
    private final int S_RGB_COLOR_SPACE = 1934772034;

    protected BmpImageData(Metadata metadata) {
        super(metadata, BmpHeaderDirectory.class);
    }

    @Override
    public int getOrientation() {
        // image same as original
        return 1;
    }

    @Override
    public int getWidth()
    {
        try
        {
            return directory.getInt(BmpHeaderDirectory.TAG_IMAGE_WIDTH);
        }
        catch (MetadataException e)
        {
            logMetadataError();
            return 0;
        }
    }

    @Override
    public int getHeight()
    {
        try {
            return directory.getInt(BmpHeaderDirectory.TAG_IMAGE_HEIGHT);
        }
        catch (MetadataException e)
        {
            logMetadataError();
            return 0;
        }
    }

    /**
     * color space type: [1934772034: sRGB, null: BnW]
     * @return number of channels 1 or 3
     */
    @Override
    public int getDepth() {
        try {
            int colorSpaceType = directory.getInt(BmpHeaderDirectory.TAG_COLOR_SPACE_TYPE);
            if (colorSpaceType == S_RGB_COLOR_SPACE)
            {
                return 3;
            }
        }
        catch (MetadataException ignored){}

        return 1;
    }

    @Override
    public String getMimeType() {
        return "image/bmp";
    }


}