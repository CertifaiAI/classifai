package ai.classifai.database.model.versiondata;

import javax.persistence.*;

@Entity
public class ImageVersionData extends DataVersion
{
    @Column(name = "img_x")
    private int imgX;

    @Column(name = "img_y")
    private int imgY;

    @Column(name = "img_w")
    private int imgW;

    @Column(name = "img_h")
    private int imgH;

}
