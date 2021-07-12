package ai.classifai.database.handler;

import ai.classifai.database.model.data.Data;
import ai.classifai.database.model.dataVersion.DataVersion;

import java.util.List;
import java.util.stream.Collectors;

public class DataVersionHandler {
    public static List<DataVersion> getDataVersionListFromDataList(List<Data> dataList)
    {
        return dataList.stream()
                .map(Data::getDataVersions)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}
