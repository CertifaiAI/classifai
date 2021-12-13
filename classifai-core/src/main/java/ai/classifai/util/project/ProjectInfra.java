package ai.classifai.util.project;

import lombok.extern.slf4j.Slf4j;

import java.util.Locale;

/**
 * State where does the project sits
 *
 * @author codenamewei
 */
@Slf4j
public enum ProjectInfra
{
    ON_PREMISE, //default
    OTHERS;  //add on if have additional service

    public static ProjectInfra get(String caseInsensitive){
        try {
            return ProjectInfra.valueOf(caseInsensitive.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            log.error("Project infra not recognized: " + caseInsensitive);
        }

        return null;
    }
}
