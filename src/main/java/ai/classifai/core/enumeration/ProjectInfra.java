package ai.classifai.core.enumeration;

import lombok.extern.slf4j.Slf4j;

import java.util.Locale;

@Slf4j
public enum ProjectInfra {
    ON_PREMISE,
    CLOUD;

    public static ProjectInfra get(String caseInsensitive){
        try {
            return ProjectInfra.valueOf(caseInsensitive.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            log.error("Project infra not recognized: " + caseInsensitive);
        }

        return null;
    }

    public static Integer getProjectInfra(String projectInfra) {
        Integer infra;

        if (projectInfra.equals(ON_PREMISE.name())) {
            infra = ON_PREMISE.ordinal();
        }

        else if (projectInfra.equals(CLOUD.name())) {
            infra = CLOUD.ordinal();
        }

        else {
            throw new IllegalArgumentException("Project Infra not found");
        }

        return infra;
    }

    public static String getProjectInfraName(String projectInfra) {
        String projectInfraName = null;

        if (projectInfra.equals(ON_PREMISE.name())) {
            projectInfraName = ON_PREMISE.name();
        }

        else if (projectInfra.equals(CLOUD.name())) {
            projectInfraName = CLOUD.name();
        }

        else {
            throw new IllegalArgumentException("Project Infra not found");
        }

        return projectInfraName;
    }

    public static ProjectInfra getType(String projectInfra) {
        ProjectInfra infra;

        if (projectInfra.equals("on_premise")) {
            infra = ON_PREMISE;
        }

        else if (projectInfra.equals("cloud")) {
            infra = CLOUD;
        }

        else {
            throw new IllegalArgumentException("Project Infra not found");
        }

        return infra;
    }
}
