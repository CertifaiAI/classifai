package ai.classifai.action;

import ai.classifai.action.parser.PortfolioParser;
import io.vertx.core.json.JsonObject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileReader;

@Slf4j
public class ProjectImport
{
    public static void importProjectFile(@NonNull File jsonFile)
    {
        try
        {
            String jsonStr = IOUtils.toString(new FileReader(jsonFile));

            JsonObject jsonObject = new JsonObject(jsonStr);

            PortfolioParser.parseIn(jsonObject);

        }
        catch(Exception e)
        {
            log.info("Error in importing project. ", e);
        }
    }
}