package ai.classifai.action.parser;

import ai.classifai.util.ParamConfig;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.NonNull;

public class PortfolioParser
{

    public static JsonObject get(@NonNull JsonObject jsonObject, @NonNull JsonArray portfolioJsonArray)
    {
        /*@Getter private static final String createPortfolioTable = "CREATE TABLE IF NOT EXISTS Portfolio (project_id UUID, project_name VARCHAR(255), annotation_type INT, " +
            "project_path VARCHAR(255), label_list VARCHAR(10000), uuid_list CLOB, is_new BOOLEAN, is_starred BOOLEAN, created_date VARCHAR(255), PRIMARY KEY (project_id))";*/


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
