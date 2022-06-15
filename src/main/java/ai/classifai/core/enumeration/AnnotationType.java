package ai.classifai.core.enumeration;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
public enum AnnotationType {
    IMAGEBOUNDINGBOX,
    IMAGESEGMENTATION,
    VIDEOBOUNDINGBOX,
    VIDEOSEGMENTATION,
    TABULAR,
    AUDIO;

    public static AnnotationType getType(@NonNull String annotation)
    {
        AnnotationType type;

        if(annotation.equals("imgbndbox"))
        {
            type = IMAGEBOUNDINGBOX;
        }
        else if(annotation.equals("imgseg"))
        {
            type = IMAGESEGMENTATION;
        }
        else if(annotation.equals("videobndbox"))
        {
            type = VIDEOBOUNDINGBOX;
        }
        else if(annotation.equals("videoseg"))
        {
            type = VIDEOSEGMENTATION;
        }
        else if(annotation.equals("tabular"))
        {
            type = TABULAR;
        }
        else if(annotation.equals("audio"))
        {
            type = AUDIO;
        }
        else
        {
            throw new IllegalArgumentException("Annotation unmatched with AnnotationType");
        }
        return type;
    }

    public static Integer getAnnotationType(String annotationType) {
        return getType(annotationType).ordinal();
    }

    public static boolean checkSanity(@NonNull Integer annotationTypeInt)
    {
        List<Integer> annotationTypeOrdinals = Arrays.stream(AnnotationType.values())
                .map(Enum::ordinal)
                .collect(Collectors.toList());

        if (annotationTypeOrdinals.contains(annotationTypeInt))
        {
            return true;
        }
        else
        {
            log.debug("Annotation unmatched in AnnotationType. " +
                    "AnnotationType only accepts [imageboundingbox/imagesegmentation/videoboundingbox/videosegmentation/tabular/audio]");
            return false;
        }
    }

    public static AnnotationType get(@NonNull String caseInsensitive)
    {
        caseInsensitive = caseInsensitive.toUpperCase(Locale.ROOT);

        if (caseInsensitive.equals(IMAGEBOUNDINGBOX.name()))
        {
            return IMAGEBOUNDINGBOX;
        }
        else if (caseInsensitive.equals(IMAGESEGMENTATION.name()))
        {
            return IMAGESEGMENTATION;
        }
        else if (caseInsensitive.equals(VIDEOBOUNDINGBOX.name()))
        {
            return VIDEOBOUNDINGBOX;
        }
        else if (caseInsensitive.equals(VIDEOSEGMENTATION.name()))
        {
            return VIDEOSEGMENTATION;
        }
        else if (caseInsensitive.equals(TABULAR.name()))
        {
            return TABULAR;
        }
        else if (caseInsensitive.equals(AUDIO.name()))
        {
            return AUDIO;
        }
        else
        {
            log.debug("Annotation type from string resulted in failure: " + caseInsensitive);
            throw new IllegalArgumentException("Annotation unmatched with AnnotationType");
        }

    }

    public static AnnotationType get(@NonNull Integer ordinal)
    {
        if (ordinal.equals(IMAGEBOUNDINGBOX.ordinal()))
        {
            return IMAGEBOUNDINGBOX;
        }
        else if (ordinal.equals(IMAGESEGMENTATION.ordinal()))
        {
            return IMAGESEGMENTATION;
        }
        else if (ordinal.equals(VIDEOBOUNDINGBOX.ordinal()))
        {
            return VIDEOBOUNDINGBOX;
        }
        else if (ordinal.equals(VIDEOSEGMENTATION.ordinal()))
        {
            return VIDEOSEGMENTATION;
        }
        else if (ordinal.equals(TABULAR.ordinal()))
        {
            return TABULAR;
        }
        else if (ordinal.equals(AUDIO.ordinal()))
        {
            return AUDIO;
        }
        else
        {
            log.debug("Annotation type from integer resulted in failure: " + ordinal);
            throw new IllegalArgumentException("Annotation unmatched with AnnotationType");
        }

    }
}
