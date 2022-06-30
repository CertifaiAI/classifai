package ai.classifai.frontend.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class LabelListBody {
    @JsonProperty("label_list")
    List<String> labelList;
}
