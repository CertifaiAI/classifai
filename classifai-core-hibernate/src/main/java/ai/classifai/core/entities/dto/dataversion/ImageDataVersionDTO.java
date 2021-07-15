package ai.classifai.core.entities.dto.dataversion;

import ai.classifai.db.entities.VersionEntity;
import ai.classifai.db.entities.data.DataEntity;
import ai.classifai.db.entities.dataVersion.ImageDataVersionEntity;
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
                .imgX(json.getFloat(ImageDataVersionEntity.IMG_X_KEY))
                .imgY(json.getFloat(ImageDataVersionEntity.IMG_Y_KEY))
                .imgW(json.getFloat(ImageDataVersionEntity.IMG_W_KEY))
                .imgH(json.getFloat(ImageDataVersionEntity.IMG_H_KEY))
                .dataId(json.getString(DataEntity.DATA_ID_KEY))
                .versionId(json.getString(VersionEntity.VERSION_ID_KEY))
                .build();
    }

    @Override
    public String toJson()
    {
        return new JsonObject()
                .put(DataEntity.DATA_ID_KEY, dataId)
                .put(VersionEntity.VERSION_ID_KEY, versionId)
                .put(ImageDataVersionEntity.IMG_X_KEY, imgX)
                .put(ImageDataVersionEntity.IMG_Y_KEY, imgY)
                .put(ImageDataVersionEntity.IMG_W_KEY, imgW)
                .put(ImageDataVersionEntity.IMG_H_KEY, imgH)
                .encodePrettily();
    }
}
