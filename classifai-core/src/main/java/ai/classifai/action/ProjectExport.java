package ai.classifai.action.export;


import ai.classifai.action.parser.BoundingBoxParser;
import ai.classifai.action.parser.ParserHelper;
import ai.classifai.action.parser.PortfolioParser;
import ai.classifai.util.ParamConfig;
import io.vertx.core.json.JsonObject;
import lombok.Builder;

import java.io.FileWriter;
import java.io.IOException;

@Builder
public class ProjectExport
{
    private PortfolioParser portfolio;
    private BoundingBoxParser bndBox;


    public boolean exportToFile()
    {
        String filePath = ParserHelper.getFilePath();

        JsonObject compiledOutput = new JsonObject()
                .put(ParamConfig.getProjectIdParam(), portfolio.getProjectID())
                .put(ParamConfig.getProjectNameParam(), portfolio.getProjectName())
                .put(ParamConfig.getAnnotationTypeParam(), portfolio.getAnnotationType())
                .put(ParamConfig.getLabelListParam(), portfolio.getLabelList())
                .put(ParamConfig.getUuidListParam(), portfolio.getUuidList())
                .put(ParamConfig.getIsNewParam(), portfolio.isNew())
                .put(ParamConfig.getIsStarredParam(), portfolio.isStar())
                .put(ParamConfig.getCreatedDateParam(), portfolio.getCreatedDate())
                .put(ParamConfig.getProjectContentParam(), bndBox.getContent());

        try {
            FileWriter file = new FileWriter(filePath);

            file.write(compiledOutput.encodePrettily());

            file.close();

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        System.out.println("JSON file created: " + filePath);

        return true;

        //false if file not created
    }

}
