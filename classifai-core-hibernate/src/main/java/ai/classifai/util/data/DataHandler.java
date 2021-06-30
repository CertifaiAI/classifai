package ai.classifai.util.data;

import ai.classifai.database.model.Project;
import ai.classifai.database.model.data.Data;

import java.util.List;

public interface DataHandler
{
    List<Data> getDataList(String projectPath);
}
