package ai.classifai.core.properties;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TabularDataProperties {
    @JsonProperty("labels")
    List<String> labels;
}
