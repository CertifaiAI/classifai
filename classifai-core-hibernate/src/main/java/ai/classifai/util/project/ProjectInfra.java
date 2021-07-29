package ai.classifai.util.project;

/**
 * State where does the project sits
 *
 * @author codenamewei
 */
public enum ProjectInfra
{
    ON_PREMISE, //default
    WASABI_S3;   //Wasabi S3 cloud

    public static ProjectInfra fromInt(int idx)
    {
        return ProjectInfra.values()[idx];
    }
}
