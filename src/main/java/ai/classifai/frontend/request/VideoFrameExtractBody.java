package ai.classifai.frontend.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class VideoFrameExtractBody {
    @JsonProperty("video_file_path")
    String videoFilePath;

    @JsonProperty("extraction_partition")
    int extractionPartition;

    @JsonProperty("extracted_frame_index")
    int extractionFrameIndex;

    @JsonProperty("current_time")
    double currentTime;
}
