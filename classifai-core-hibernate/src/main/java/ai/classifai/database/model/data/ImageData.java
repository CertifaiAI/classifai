package ai.classifai.database.model.data;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@Getter
@Setter
@Entity
public class ImageData extends Data
{
    private int depth;

    private int width;

    private int height;

    private String thumbnail;
}
