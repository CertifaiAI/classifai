package ai.classifai.db.handler;

import ai.classifai.db.entities.data.DataEntity;
import ai.classifai.db.entities.dataVersion.DataVersionEntity;

import java.util.List;
import java.util.stream.Collectors;

public class DataVersionHandler {
    public static List<DataVersionEntity> getDataVersionListFromDataList(List<DataEntity> dataEntityList)
    {
        return dataEntityList.stream()
                .map(DataEntity::getDataVersionEntities)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}
