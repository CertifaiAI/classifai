package ai.classifai.core.dto.properties;

import ai.classifai.backend.data.handler.ImageHandler;
import ai.classifai.backend.utility.ParamConfig;
import ai.classifai.backend.utility.UuidGenerator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class ImageProperties {
    @JsonProperty
    String projectId;

    @JsonProperty
    String projectName;

    @JsonProperty
    String projectPath;

    @JsonProperty
    String imgUuid;

    @JsonProperty
    String imgPath;

    @JsonProperty
    int imgOriginalWidth;

    @JsonProperty
    int imgOriginalHeight;

    @JsonProperty
    int imgDepth;

    @JsonProperty
    String imgBase64;

    @JsonProperty
    Long fileSize;

    public static ImageProperties getImageProperties(ImageProperties imageProperties, File imageFile) throws Exception {
        Map<String, String> imageMetaData = ImageHandler.getImageMetaData(imageFile);

        imageProperties.setFileSize(FileUtils.sizeOf(imageFile));
        imageProperties.setImgUuid(UuidGenerator.generateUuid());
        imageProperties.setImgOriginalHeight(Integer.parseInt(imageMetaData.get(ParamConfig.getImgOriHParam())));
        imageProperties.setImgOriginalWidth(Integer.parseInt(imageMetaData.get(ParamConfig.getImgOriWParam())));
        imageProperties.setImgDepth(Integer.parseInt(imageMetaData.get(ParamConfig.getImgOriWParam())));
        imageProperties.setImgBase64(imageMetaData.get(ParamConfig.getImgThumbnailParam()));
        imageProperties.setImgPath(imageFile.getPath());

        return imageProperties;
    }
}
