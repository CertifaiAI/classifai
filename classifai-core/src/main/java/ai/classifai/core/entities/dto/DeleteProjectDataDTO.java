package ai.classifai.core.entities.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DeleteProjectDataDTO {
    @JsonProperty("uuid_list")
    List<String> uuidList;

    @JsonProperty("img_path_list")
    List<String> imgPathList;
}
