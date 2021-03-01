package ai.classifai.action;

import ai.classifai.util.datetime.DateTime;
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
    public static JsonObject getDefaultJsonObject()
    {
        JsonObject jsonObject = new JsonObject()
                .put("tool", "classifai")
                .put("tool version", "2.0.0-alpha")
                .put("updateddate", new DateTime().toString());


        return jsonObject;
    }

    public static boolean exportToFile(@NonNull File jsonPath, @NonNull JsonObject jsonObject)
    {

        try {
            FileWriter file = new FileWriter(jsonPath);

            file.write(jsonObject.encodePrettily());

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
