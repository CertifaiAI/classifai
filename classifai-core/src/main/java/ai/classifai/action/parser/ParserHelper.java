package ai.classifai.action.parser;

import ai.classifai.util.type.AnnotationHandler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.Getter;

import java.io.File;

public class ParserHelper
{
    @Getter private static final String filePath = System.getProperty("user.home") + File.separator + "classifai.json";

    public static PortfolioParser buildPortfolioParser(JsonArray jsonArray)
    {
        int counter = 0;

        PortfolioParser portfolio = PortfolioParser.builder()
                .projectID(jsonArray.getInteger(counter++))
                .projectName(jsonArray.getString(counter++))
                .annotationType(AnnotationHandler.getType(jsonArray.getInteger(counter++)).name())
                .labelList(jsonArray.getString(counter++))
                .uuidGeneratorSeed(jsonArray.getInteger(counter++))
                .uuidList(jsonArray.getString(counter++))
                .isNew(jsonArray.getBoolean(counter++))
                .isStar(jsonArray.getBoolean(counter++))
                .createdDate(jsonArray.getString(counter))
                .build();

        return portfolio;
    }

    public static BoundingBoxParser buildBndBoxParser(JsonObject jsonObject)
    {
        BoundingBoxParser bndBox = BoundingBoxParser.builder()
                .content(jsonObject)
                .build();

        return bndBox;
    }



}
