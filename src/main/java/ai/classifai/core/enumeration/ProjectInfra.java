package ai.classifai.core.enumeration;

import lombok.extern.slf4j.Slf4j;

import java.util.Locale;

@Slf4j
public enum ProjectInfra {
    ONPREMISE,
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
        Integer infra = null;

        if (projectInfra.equals(ONPREMISE.name())) {
            infra = ONPREMISE.ordinal();
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

        if (projectInfra.equals(ONPREMISE.name())) {
            projectInfraName = ONPREMISE.name();
        }

        else if (projectInfra.equals(CLOUD.name())) {
            projectInfraName = CLOUD.name();
        }

        else {
            throw new IllegalArgumentException("Project Infra not found");
        }

        return projectInfraName;
    }
}
