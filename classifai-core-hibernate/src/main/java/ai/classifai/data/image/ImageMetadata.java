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

import ai.classifai.util.exception.NotSupportedImageTypeException;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.bmp.BmpHeaderDirectory;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.png.PngDirectory;
import com.drew.metadata.webp.WebpDirectory;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import static com.drew.metadata.exif.ExifIFD0Directory.TAG_ORIENTATION;
import static java.lang.Math.PI;

/**
 * ImageData provides metadata of images
 *
 * @author YCCertifai
 */
@Slf4j
public abstract class ImageMetadata
{
    private static final ImageDataFactory FACTORY = new ImageDataFactory();
    protected Metadata metadata;
    protected Directory directory;
    private final String MIME_TYPE;

    protected <T extends Directory> ImageMetadata(Metadata metadata, Class<T> directoryClass, String mimeType)
    {
        this.metadata = metadata;
        this.directory = metadata.getFirstDirectoryOfType(directoryClass);
        this.MIME_TYPE = mimeType;
    }

    protected abstract int getRawWidth();

    protected abstract int getRawHeight();

    public abstract int getDepth();

    public abstract boolean isAnimation();

    public String getBase64Header()
    {
        return String.format("data:%s;base64,", MIME_TYPE);
    }

    protected void logMetadataError()
    {
        log.error("Unhandled metadata error, this should be protected by ImageFactory");
    }

    public int getWidth()
    {
        int orientation = getOrientation();

        if (orientation == 8 || orientation == 6)
        {
            return getRawHeight();
        }

        return getRawWidth();
    }

    public int getHeight()
    {
        int orientation = getOrientation();

        if (orientation == 8 || orientation == 6)
        {
            return getRawWidth();
        }

        return getRawHeight();
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
    public int getOrientation()
    {
        try
        {
            return metadata.getFirstDirectoryOfType(ExifIFD0Directory.class).getInt(TAG_ORIENTATION);
        } catch (Exception ignored)
        {
            // if can't find orientation set as 0 deg
            return 1;
        }
    }

    public double getAngle()
    {
        return switch (getOrientation()) {
            case 8 -> -0.5 * PI;
            case 3 -> PI;
            case 1 -> 0.5 * PI;
            default -> 0;
        };
    }

    public static ImageMetadata getImageMetadata(File filePath)
    {
        try
        {
            Metadata metadata = ImageMetadataReader.readMetadata(filePath);

            return FACTORY.getImageData(metadata);
        }
        catch (ImageProcessingException | IOException e)
        {
            log.debug(String.format("%s is not an image", filePath));
            return null;
        }
        catch (NotSupportedImageTypeException e)
        {
            log.debug(String.format("%s is not supported %n %s", filePath, e.getMessage()));
            return null;
        }
    }

    public static ImageMetadata getImageMetadata(byte [] bytes)
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

    public static class ImageDataFactory
    {
        private ImageDataFactory() {}

        public ImageMetadata getImageData(Metadata metadata) throws NotSupportedImageTypeException
        {
            if (metadata.containsDirectoryOfType(JpegDirectory.class)) {
                return new JpegImageMetadata(metadata);
            } else if (metadata.containsDirectoryOfType(PngDirectory.class)) {
                return new PngImageMetadata(metadata);
            } else if (metadata.containsDirectoryOfType(BmpHeaderDirectory.class)) {
                return new BmpImageMetadata(metadata);
            } else if (metadata.containsDirectoryOfType(WebpDirectory.class)) {
                return new WebpImageMetadata(metadata);
            } else {
                throw new NotSupportedImageTypeException(String.format("%s type not supported", metadata.getDirectories()));
            }
        }
    }
}