package ai.classifai.database.model.data;

import ai.classifai.database.model.Project;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "IMAGE")
public class Image extends Data
{
    @Column(name = "img_depth")
    private int depth;

    @Column(name = "img_width")
    private int width;

    @Column(name = "img_height")
    private int height;

    @Column(name = "thumbnail")
    private String thumbnail;

    public Image(String dataPath, String checksum, long fileSize, int depth, int width, int height)
    {
        super(dataPath, checksum, fileSize);
        this.depth = depth;
        this.width = width;
        this.height = height;
    }

    public Image() {}
}
