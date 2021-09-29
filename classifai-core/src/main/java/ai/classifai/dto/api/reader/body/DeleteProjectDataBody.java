package ai.classifai.dto.api.reader.body;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DeleteProjectDataBody {
    @JsonProperty("uuid_list")
    List<String> uuidList;

    @JsonProperty("img_path_list")
    List<String> imgPathList;
}
