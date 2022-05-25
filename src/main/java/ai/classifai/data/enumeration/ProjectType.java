package ai.classifai.data.enumeration;

import lombok.NonNull;

public enum ProjectType {
    IMAGEBOUNDINGBOX,
    IMAGESEGMENTATION,
    VIDEOBOUNDINGBOX,
    VIDEOSEGMENTATION,
    TABULAR,
    AUDIO;

    public static ProjectType getProjectType(@NonNull String annotationType)
    {
        ProjectType projectType = null;

        switch (annotationType) {
            case "bndbox" -> {
                projectType = IMAGEBOUNDINGBOX;
            }
            case "seg" -> {
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

        return  projectType;
    }
}
