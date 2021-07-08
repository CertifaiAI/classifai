package ai.classifai.data.type.image;

import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.webp.WebpDirectory;

import static com.drew.metadata.webp.WebpDirectory.TAG_IS_ANIMATION;

public class WebpImageData extends ImageData{

    protected WebpImageData(Metadata metadata)
    {
        super(metadata, WebpDirectory.class);
    }

    private int getRawWidth()
    {
        try {
            return directory.getInt(WebpDirectory.TAG_IMAGE_WIDTH);
        } catch (MetadataException e) {
            logMetadataError();
            return 0;
        }
    }

    private int getRawHeight()
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
    public String getMimeType() {
        return "image/webp";
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
