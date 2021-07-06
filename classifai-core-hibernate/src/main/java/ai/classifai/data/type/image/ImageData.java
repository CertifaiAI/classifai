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
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.bmp.BmpHeaderDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.png.PngDirectory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

/**
 * ImageData provides metadata of images
 *
 * @author YCCertifai
 */
@Slf4j
public abstract class ImageData
{
    private static final ImageDataFactory FACTORY = new ImageDataFactory();
    protected Metadata metadata;
    protected Directory directory;

    protected <T extends Directory> ImageData(Metadata metadata, Class<T> directoryClass)
    {
        this.metadata = metadata;
        this.directory = metadata.getFirstDirectoryOfType(directoryClass);
    }

    public abstract int getWidth();
    public abstract int getHeight();
    public abstract int getDepth();
    public abstract String getMimeType();

    public String getBase64Header()
    {
        return String.format("data:%s;base64,", getMimeType());
    }

    public static ImageData getImageData(String filePath)
    {
        try
        {
            Metadata metadata = ImageMetadataReader.readMetadata(new File(filePath));

            return FACTORY.getImageData(metadata);
        }
        catch (ImageProcessingException | IOException e)
        {
            log.debug(String.format("%s is not an image", filePath));
            return null;
        }
        catch (NotSupportedImageTypeException e)
        {
            log.debug(String.format("%s is not supported \n %s", filePath, e.getMessage()));
            return null;
        }
    }

    public static ImageData getImageData(byte[] bytes)
    {
        try
        {
            Metadata metadata = ImageMetadataReader.readMetadata(new ByteArrayInputStream(bytes));

            return FACTORY.getImageData(metadata);
        }
        catch (ImageProcessingException | IOException e)
        {
            log.debug("byte array received is not an image");
            return null;
        }
        catch (NotSupportedImageTypeException e)
        {
            log.debug("byte array received is not supported");
            return null;
        }
    }

    protected void logMetadataError()
    {
        log.error("Unhandled metadata error, this should be protected by ImageFactory");
    }

    public static class ImageDataFactory
    {
        private ImageDataFactory() {}

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
}