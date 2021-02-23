package ai.classifai.action;

import ai.classifai.action.parser.PortfolioParser;

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