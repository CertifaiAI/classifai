package ai.classifai.data.enumeration;

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

        return  projectType.ordinal();
    }
}
