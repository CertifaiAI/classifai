package ai.classifai.service;

import ai.classifai.core.entity.dto.generic.DataDTO;
import ai.classifai.core.entity.model.generic.Project;
import ai.classifai.core.entity.model.generic.Data;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.List;
import java.util.stream.Collectors;

public class DataService extends FileService
{
    public DataService(Vertx vertx) {
        super(vertx);
    }

    private Boolean isDataValid(Data data)
    {
        return isPathExists(data.getPath()) && isChecksumMatch(data.getChecksum(), data.getPath());
    }

    public Future<List<Data>> filterValidData(List<Data> dataList)
    {
        return Future.succeededFuture(dataList.stream()
                .filter(this::isDataValid)
                .collect(Collectors.toList()));
    }


    public Future<List<DataDTO>> getToAddDataDtoList(List<Data> dataList, Project project)
    {
        return null;
    }

    public Future<List<DataDTO>> getDataDtoList(String projectPath, AnnotationType type)
    {
        return null;
    }
}
