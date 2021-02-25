package ai.classifai.action.parser;

import ai.classifai.util.ParamConfig;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.NonNull;

public class PortfolioParser
{

    public static JsonObject get(@NonNull JsonObject jsonObject, @NonNull JsonArray portfolioJsonArray)
    {
        jsonObject.put(ParamConfig.getProjectIdParam(), portfolioJsonArray.getString(0));
        jsonObject.put(ParamConfig.getProjectNameParam(), portfolioJsonArray.getString(1));
        jsonObject.put(ParamConfig.getAnnotationTypeParam(), portfolioJsonArray.getInteger(2));

        jsonObject.put(ParamConfig.getProjectPathParam(), portfolioJsonArray.getString(3));
        jsonObject.put(ParamConfig.getLabelListParam(), portfolioJsonArray.getString(4));

        jsonObject.put(ParamConfig.getUuidListParam(), portfolioJsonArray.getString(5));
        jsonObject.put(ParamConfig.getIsNewParam(), portfolioJsonArray.getBoolean(6));
        jsonObject.put(ParamConfig.getIsStarredParam(), portfolioJsonArray.getBoolean(7));

        jsonObject.put(ParamConfig.getCreatedDateParam(), portfolioJsonArray.getString(8));


        return jsonObject;
    }
}
