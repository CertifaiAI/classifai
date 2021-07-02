package ai.classifai.database.model.dataVersion;

import ai.classifai.database.model.Version;
import ai.classifai.database.model.data.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "IMAGE_DATA_VERSION")
public class ImageDataVersion extends DataVersion
{
    @Column(name = "img_x")
    private int imgX;

    @Column(name = "img_y")
    private int imgY;

    @Column(name = "img_w")
    private int imgW;

    @Column(name = "img_h")
    private int imgH;

    public ImageDataVersion(Data data, Version version)
    {
        super(data, version);
        imgX = 0;
        imgY = 0;
        imgW = 0;
        imgH = 0;
    }

    public ImageDataVersion() {}
}
