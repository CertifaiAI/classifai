package ai.classifai.core.properties.tabular;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TabularProperties {
    String projectId;

    String projectName;

    String projectPath;

    List<TabularDataProperties> tabularDataPropertiesList;
}
