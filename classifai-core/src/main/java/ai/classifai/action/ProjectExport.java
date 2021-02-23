package ai.classifai.action;

import ai.classifai.action.parser.AnnotationParser;
import ai.classifai.action.parser.PortfolioParser;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.util.DateTime;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Builder
@Slf4j
public class ProjectExport
{
    private static JsonObject getDefaultJsonObject()
    {
        JsonObject jsonObject = new JsonObject()
                .put("tool", "classifai")
                .put("version", "2.0.0-alpha")
                .put("updateddate", DateTime.get());


        return jsonObject;
    }

    public static boolean exportToFile(@NonNull File jsonPath, ProjectLoader loader)
    {
        JsonObject compiledOutput = getDefaultJsonObject();

        return saveToFile(jsonPath, compiledOutput);
    }


    public static boolean exportToFile(@NonNull File jsonPath, JsonArray portfolioJsonArray, JsonArray annotationJsonArray)
    {
        JsonObject compiledOutput = getDefaultJsonObject();

        if(portfolioJsonArray != null)
        {
            compiledOutput = PortfolioParser.get(compiledOutput, portfolioJsonArray);
        }

        if(annotationJsonArray != null)
        {
            compiledOutput = AnnotationParser.get(compiledOutput, annotationJsonArray);
        }

        return saveToFile(jsonPath, compiledOutput);

        //false if file not created
    }


    private static boolean saveToFile(@NonNull File jsonPath, JsonObject jsonObj)
    {
        try {
            FileWriter file = new FileWriter(jsonPath);

            file.write(jsonObj.encodePrettily());

            file.close();

        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }

        log.info("Project configuration file saved at: " + jsonPath);

        return true;
    }
}
