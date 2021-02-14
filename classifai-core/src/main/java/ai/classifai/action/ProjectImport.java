package ai.classifai.action;

import ai.classifai.action.parser.ParserHelper;
import ai.classifai.action.parser.PortfolioParser;
import io.vertx.core.json.JsonObject;
import org.apache.commons.io.IOUtils;

import java.io.FileReader;

public class ProjectImport
{
    private PortfolioParser portfolio;

    public boolean importFromFile()
    {
        //String filePath = ParserHelper.getFilePath();

        try
        {
            //String jsonStr = IOUtils.toString(new FileReader(filePath));
            //JsonObject jsonObject = new JsonObject(jsonStr);

        }
        catch(Exception e)
        {
            System.out.println(e);
        }



        return true;
    }
}