package ai.classifai.db.entities.annotation;

import ai.classifai.core.entities.ImageAnnotation;
import ai.classifai.core.entities.dto.annotation.AnnotationDTO;
import ai.classifai.db.entities.PointEntity;
import ai.classifai.db.entities.annotation.AnnotationEntity;
import ai.classifai.db.entities.dataVersion.DataVersionEntity;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity(name = "IMAGE_ANNOTATION")
@Inheritance(strategy = InheritanceType.JOINED)
public class ImageAnnotationEntity extends AnnotationEntity implements ImageAnnotation
{
    @OneToMany(mappedBy = "annotation")
    private List<PointEntity> points;

    @Override
    public AnnotationDTO toDTO() {
        return null;
    }
}
