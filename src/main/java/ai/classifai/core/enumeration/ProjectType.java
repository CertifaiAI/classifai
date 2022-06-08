package ai.classifai.core.enumeration;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum ProjectType {
    IMAGEBOUNDINGBOX,
    IMAGESEGMENTATION,
    VIDEOBOUNDINGBOX,
    VIDEOSEGMENTATION,
    TABULAR,
    AUDIO;

    public static Integer getProjectType(@NonNull String annotationType)
    {
        ProjectType projectType = null;

        switch (annotationType) {
            case "imgbndbox" -> {
                projectType = IMAGEBOUNDINGBOX;
            }
            case "imgseg" -> {
                projectType = IMAGESEGMENTATION;
            }
            case "videobndbox" -> {
                projectType = VIDEOBOUNDINGBOX;
            }
            case "videoseg" -> {
                projectType = VIDEOSEGMENTATION;
            }
            case "tabular" -> {
                projectType = TABULAR;
            }
            case "audio" -> {
                projectType = AUDIO;
            }
        }

        return projectType.ordinal();
    }

    public static String getProjectTypeName(@NonNull Integer projectTypeEnum) {
        String projectTypeName = null;

        switch (projectTypeEnum) {
            case 0 -> {
                projectTypeName = IMAGEBOUNDINGBOX.name();
            }
            case 1 -> {
                projectTypeName = IMAGESEGMENTATION.name();
            }
            case 2 -> {
                projectTypeName = VIDEOBOUNDINGBOX.name();
            }
            case 3 -> {
                projectTypeName = VIDEOSEGMENTATION.name();
            }
            case 4 -> {
                projectTypeName = TABULAR.name();
            }
            case 5 -> {
                projectTypeName = AUDIO.name();
            }
        }

        return projectTypeName;
    }
}
