package ai.classifai.dto.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
public class TabularDataProperties {
    @JsonProperty("labels")
    List<String> labels;
}
