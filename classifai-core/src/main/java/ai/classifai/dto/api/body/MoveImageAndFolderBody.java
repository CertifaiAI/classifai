package ai.classifai.dto.api.body;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class MoveImageAndFolderBody {
    @JsonProperty("modify_name")
    Boolean modifyImageOrFolderName;

    @JsonProperty("replace_name")
    Boolean replaceImageOrFolder;

    @JsonProperty("img_path_list")
    List<String> imagePathList;

    @JsonProperty("img_directory_list")
    List<String> imageDirectoryList;
}
