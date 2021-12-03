package ai.classifai.dto.api.body;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class AddImageBody {
    @JsonProperty("img_name_list")
    List<String> imgNameList;

    @JsonProperty("img_base64_list")
    List<String> imgBase64List;
}
