package ai.classifai.entities.dto.dataversion;

import ai.classifai.database.model.Version;
import ai.classifai.database.model.dataVersion.ImageDataVersion;
import ai.classifai.database.model.data.Data;
import io.vertx.core.json.JsonObject;
import lombok.*;

import java.io.Serial;

@lombok.Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ImageDataVersionDTO extends DataVersionDTO
{

    @Serial
    private static final long serialVersionUID = 1357120222631740426L;

    private float imgX;
    private float imgY;
    private float imgW;
    private float imgH;

    @Builder
    public ImageDataVersionDTO(String dataId, String versionId, float imgX, float imgY, float imgW, float imgH)
    {
        super(dataId, versionId);
        this.imgX = imgX;
        this.imgY = imgY;
        this.imgW = imgW;
        this.imgH = imgH;
    }


    @Override
    public DataVersionDTO readJson(String jsonString)
    {
        JsonObject json = new JsonObject(jsonString);

        return ImageDataVersionDTO.builder()
                .imgX(json.getFloat(ImageDataVersion.IMG_X_KEY))
                .imgY(json.getFloat(ImageDataVersion.IMG_Y_KEY))
                .imgW(json.getFloat(ImageDataVersion.IMG_W_KEY))
                .imgH(json.getFloat(ImageDataVersion.IMG_H_KEY))
                .dataId(json.getString(Data.DATA_ID_KEY))
                .versionId(json.getString(Version.VERSION_ID_KEY))
                .build();
    }

    @Override
    public String toJson()
    {
        return new JsonObject()
                .put(Data.DATA_ID_KEY, dataId)
                .put(Version.VERSION_ID_KEY, versionId)
                .put(ImageDataVersion.IMG_X_KEY, imgX)
                .put(ImageDataVersion.IMG_Y_KEY, imgY)
                .put(ImageDataVersion.IMG_W_KEY, imgW)
                .put(ImageDataVersion.IMG_H_KEY, imgH)
                .encodePrettily();
    }
}
