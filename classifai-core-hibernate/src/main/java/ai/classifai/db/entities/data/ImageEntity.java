package ai.classifai.db.entities.data;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "IMAGE")
public class ImageEntity extends DataEntity
{
    @Column(name = "img_depth")
    private int depth;

    @Column(name = "img_width")
    private int width;

    @Column(name = "img_height")
    private int height;
}
