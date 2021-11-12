package ai.classifai.core.entities.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class LabelListDTO {
    @JsonProperty("label_list")
    List<String> labelList;
}
