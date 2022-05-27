package ai.classifai.data.enumeration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum ProjectInfra {
    ONPREMISE,
    CLOUD;

    public static Integer getProjectInfra(String projectInfra) {
        ProjectInfra infra = null;

        switch(projectInfra) {
            case "on_premise" -> infra = ONPREMISE;
            case "cloud" -> infra = CLOUD;
        }

        return infra.ordinal();
    }
}
