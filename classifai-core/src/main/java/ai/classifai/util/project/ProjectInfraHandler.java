package ai.classifai.util.project;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Locale;

/**
 * Check for project infrastructure
 *
 * Reminder to add here when have new infra type
 *
 * @author codenamewei
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProjectInfraHandler
{
    public static ProjectInfra getInfra(@NonNull String strInfra)
    {
        strInfra = strInfra.toUpperCase(Locale.ROOT);

        ProjectInfra infra = null;

        if(strInfra.equals(ProjectInfra.ON_PREMISE.name()))
        {
            infra = ProjectInfra.ON_PREMISE;
        }
        else if(strInfra.equals(ProjectInfra.WASABI_S3.name()))
        {
            infra = ProjectInfra.WASABI_S3;
        }
        else
        {
            log.info("Project infra not recognized: " + strInfra);
        }

        return infra;

    }
}
