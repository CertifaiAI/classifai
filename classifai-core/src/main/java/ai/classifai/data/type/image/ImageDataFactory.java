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

import ai.classifai.util.exception.NotSupportedImageTypeException;
import com.drew.metadata.Metadata;
import com.drew.metadata.bmp.BmpHeaderDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.png.PngDirectory;

/**
 * ImageDataFactory to return subclass of ImageData based on metadata
 * throw NotSupportedImageTypeError if image type is not supported
 *
 * @author YCCertifai
 */
public class ImageDataFactory
{
    public ImageData getImageData(Metadata metadata) throws NotSupportedImageTypeException
    {
        if (metadata.containsDirectoryOfType(JpegDirectory.class))
        {
            return new JpegImageData(metadata);
        }
        else if (metadata.containsDirectoryOfType(PngDirectory.class))
        {
            return new PngImageData(metadata);
        }
        else if (metadata.containsDirectoryOfType(BmpHeaderDirectory.class))
        {
            return new BmpImageData(metadata);
        }
        else
        {
            throw new NotSupportedImageTypeException(String.format("%s type not supported", metadata.getDirectories()));
        }
    }
}