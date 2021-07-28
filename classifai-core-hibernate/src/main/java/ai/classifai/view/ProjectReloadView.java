package ai.classifai.view;

import ai.classifai.core.entity.dto.generic.DataDTO;
import ai.classifai.selector.status.FileSystemStatus;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.http.HTTPResponseHandler;
import ai.classifai.util.message.ReplyHandler;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectReloadView
{
    public JsonObject generate(List<DataDTO> addedDataDTOList)
    {
        return new JsonObject()
                .put("uuid_add_list", getUuidStringList(addedDataDTOList));
    }

    public JsonObject generateStatus(List<DataDTO> addedDataDTOList)
    {
        JsonObject response = ReplyHandler.getOkReply();

        return response.put(ParamConfig.getFileSysStatusParam(), FileSystemStatus.DATABASE_UPDATED.ordinal())
                .put(ParamConfig.getFileSysMessageParam(), FileSystemStatus.DATABASE_UPDATED.name())
                .put("uuid_add_list", getUuidStringList(addedDataDTOList));
    }

    private List<String> getUuidStringList(List<DataDTO> addedDataDTOList) {
        return addedDataDTOList.stream()
                .map(dataDTO -> dataDTO.getId().toString())
                .collect(Collectors.toList());
    }
}
