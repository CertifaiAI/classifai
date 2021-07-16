package ai.classifai.db.entities;

import ai.classifai.core.entities.Annotation;
import ai.classifai.core.entities.Point;
import ai.classifai.core.entities.dto.PointDTO;
import ai.classifai.db.entities.annotation.AnnotationEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

// FIXME:
//  abstract out for every image
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class PointEntity implements Point
{
    public static final String POINT_ID = "point_id";
    public static final String X = "x";
    public static final String Y = "y";
    public static final String DIST2IMG_X = "dist_2_img_x";
    public static final String DIST2IMG_Y = "dist_2_img_y";

    @Id
    @Column(name = POINT_ID)
    private UUID id;

    @Column(name = X)
    private Float x;

    @Column(name = Y)
    private Float y;

    @Column(name = DIST2IMG_X)
    private Float dist2ImgX;

    @Column(name = DIST2IMG_Y)
    private Float dist2ImgY;

    private Integer position;

    @ManyToOne
    @JoinColumn(name = AnnotationEntity.ANNOTATION_ID_KEY)
    private AnnotationEntity annotation;

    @Override
    public PointDTO toDTO() {
        return null;
    }



}
