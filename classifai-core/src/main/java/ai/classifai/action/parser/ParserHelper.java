package ai.classifai.action.parser;

import ai.classifai.loader.ProjectLoader;
import ai.classifai.util.ProjectHandler;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.NonNull;

import java.io.File;

public class ParserHelper
{
    public static BoundingBoxParser buildBndBoxParser(JsonObject jsonObject)
    {
        BoundingBoxParser bndBox = BoundingBoxParser.builder()
                .content(jsonObject)
                .build();

        return bndBox;
    }

    public static String getProjectExportPath(@NonNull String projectId)
    {
        ProjectLoader loader = (ProjectLoader) ProjectHandler.getProjectLoader(projectId);

        return loader.getProjectPath() + File.separator + loader.getProjectName() + ".json";
    }


}

