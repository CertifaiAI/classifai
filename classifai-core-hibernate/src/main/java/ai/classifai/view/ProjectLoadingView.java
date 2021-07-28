package ai.classifai.view;

import ai.classifai.core.entity.dto.generic.DataDTO;
import ai.classifai.core.entity.dto.generic.LabelDTO;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProjectLoadingView
{
    public JsonObject generateLoadProjectView(List<LabelDTO> labelDTOList, List<DataDTO> dataDTOList)
    {
        List<String> labelNameList = labelDTOList.stream()
                .map(LabelDTO::getName)
                .collect(Collectors.toList());

        List<String> dataIdList = dataDTOList.stream()
                .map(DataDTO::getId)
                .map(Objects::toString)
                .collect(Collectors.toList());

        return new JsonObject().put("message", 1)
                .put("label_list", new JsonArray(labelNameList))
                .put("uuid_list", new JsonArray(dataIdList));
    }

    // FIXME: to be deleted
    public JsonObject generateLoadProjectViewStatus(List<LabelDTO> labelDTOList, List<DataDTO> dataDTOList)
    {
        return generateLoadProjectView(labelDTOList, dataDTOList)
                .put("message", 2);
    }
}
