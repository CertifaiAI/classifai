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
package ai.classifai.core.data.type.image;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.bmp.BmpHeaderDirectory;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.png.PngDirectory;
import com.drew.metadata.webp.WebpDirectory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import static com.drew.metadata.exif.ExifIFD0Directory.TAG_ORIENTATION;

/**
 * ImageData provides metadata of images
 *
 * @author YCCertifai
 */
@Slf4j
public abstract class ImageData
{

    protected Metadata metadata;
    protected Directory directory;
    @Getter protected String mimeType;

    protected <T extends Directory> ImageData(Metadata metadata, Class<T> directoryClass, String mimeType) {
        this.metadata = metadata;
        this.directory = metadata.getFirstDirectoryOfType(directoryClass);
        this.mimeType = mimeType;
    }

    protected abstract int getRawWidth();

    protected abstract int getRawHeight();

    public abstract int getDepth();

    public abstract boolean isAnimation();

    protected void logMetadataError() {
        log.error("Unhandled metadata error, this should be protected by ImageFactory");
    }

    public int getWidth() {
        int orientation = getOrientation();

        if (orientation == 8 || orientation == 6) {
            return getRawHeight();
        }

        return getRawWidth();
    }

    public int getHeight() {
        int orientation = getOrientation();

        if (orientation == 8 || orientation == 6) {
            return getRawWidth();
        }

        return getRawHeight();
    }

    public int getOrientation() {
        try {
            return metadata.getFirstDirectoryOfType(ExifIFD0Directory.class).getInt(TAG_ORIENTATION);
        } catch (Exception ignored) {
            // if can't find orientation set as 0 deg
            return 1;
        }
    }

    public static ImageData getImageData(File filePath) throws Exception {
        try
        {
            Metadata metadata = ImageMetadataReader.readMetadata(filePath);

            return ImageDataFactory.getImageData(metadata);
        }
        catch (ImageProcessingException | IOException e)
        {
            log.debug(String.format("%s is not an image", filePath));
            return null;
        }
    }

    public static ImageData getImageData(byte [] bytes) throws Exception {
        try
        {
            Metadata metadata = ImageMetadataReader.readMetadata(new ByteArrayInputStream(bytes));

            return ImageDataFactory.getImageData(metadata);
        }
        catch (ImageProcessingException | IOException e)
        {
            log.debug("byte array received is not an image");
            return null;
        }
    }

    public static class ImageDataFactory {
        private ImageDataFactory() {
        }

        public static ImageData getImageData(Metadata metadata) throws Exception {
            if (metadata.containsDirectoryOfType(JpegDirectory.class)) {
                return new JpegImageData(metadata);
            } else if (metadata.containsDirectoryOfType(PngDirectory.class)) {
                return new PngImageData(metadata);
            } else if (metadata.containsDirectoryOfType(BmpHeaderDirectory.class)) {
                return new BmpImageData(metadata);
            } else if (metadata.containsDirectoryOfType(WebpDirectory.class)) {
                return new WebpImageData(metadata);
            } else {
                throw new Exception(String.format("%s type not supported", metadata.getDirectories()));
            }
        }
    }

    public double getAngle() {
        double angle;

        if (getOrientation() == 8)
        {
            angle = -0.5*Math.PI; // the image turn 270 degree
        }
        else if (getOrientation() == 3)
        {
            angle = Math.PI; // the image turn 180 degree
        }
        else if (getOrientation() == 6)
        {
            angle = 0.5*Math.PI; // the image turn 90 degree
        }
        else
        {
            angle = 0;
        }

        return angle;
    }
}