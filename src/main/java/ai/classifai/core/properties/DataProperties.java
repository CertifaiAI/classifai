package ai.classifai.core.properties;

public abstract class DataProperties {
    String projectPath;

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    public abstract void parseData(String path);
}
