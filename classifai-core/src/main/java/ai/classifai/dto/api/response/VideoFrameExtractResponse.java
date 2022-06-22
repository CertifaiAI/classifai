package ai.classifai.dto.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoFrameExtractResponse {
    @JsonProperty("message")
    int message;

    @JsonProperty("error_message")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String errorMessage;

    @JsonProperty("current_time_stamp")
    int currentTimeStamp;

    @JsonProperty("is_video_frames_extraction_completed")
    boolean isVideoFrameExtractionCompleted;

    @JsonProperty("extracted_frame_index")
    int extractedFrameIndex;

    @JsonProperty("video_frames_extraction_status")
    int videoFrameExtractionStatus;

    @JsonProperty("video_frames_extraction_message")
    String videoFrameExtractionMessage;
}
