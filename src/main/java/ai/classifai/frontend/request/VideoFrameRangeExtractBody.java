package ai.classifai.frontend.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class VideoFrameRangeExtractBody {
    @JsonProperty("video_file_path")
    String videoFilePath;

    @JsonProperty("extraction_start_time")
    Double extractionStartTime;

    @JsonProperty("extraction_end_time")
    Double extractionEndTime;

    @JsonProperty("extraction_partition")
    Integer extractionPartition;
}
