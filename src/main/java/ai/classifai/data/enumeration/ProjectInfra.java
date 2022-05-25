package ai.classifai.data.enumeration;

public enum ProjectInfra {
    ONPREMISE,
    CLOUD;

    public static ProjectInfra getProjectInfra(String projectInfra) {
        ProjectInfra infra = null;

        switch(projectInfra) {
            case "on_premise" -> infra = ONPREMISE;
            case "cloud" -> infra = CLOUD;
        }

        return infra;
    }
}
