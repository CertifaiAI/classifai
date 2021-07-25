package ai.classifai.database.entity.image.annotation;

import ai.classifai.core.entity.dto.image.annotation.ImageAnnotationDTO;
import ai.classifai.core.entity.model.generic.Point;
import ai.classifai.core.entity.model.image.annotation.ImageAnnotation;
import ai.classifai.database.entity.generic.PointEntity;
import ai.classifai.database.entity.generic.AnnotationEntity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "image_annotation")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ImageAnnotationEntity extends AnnotationEntity implements ImageAnnotation
{
    @OneToMany(mappedBy = "annotation",
            cascade = CascadeType.ALL)
    List<PointEntity> pointList;

    @Override
    public List<Point> getPointList()
    {
        return new ArrayList<>(pointList);
    }

    public void addPoint(PointEntity point)
    {
        point.setAnnotation(this);
        pointList.add(point);
    }
}
