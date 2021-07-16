package ai.classifai.db.entities.annotation;

import ai.classifai.core.entities.Annotation;
import ai.classifai.core.entities.Point;
import ai.classifai.core.entities.dto.annotation.AnnotationDTO;
import ai.classifai.db.entities.LabelEntity;
import ai.classifai.db.entities.ProjectEntity;
import ai.classifai.db.entities.VersionEntity;
import ai.classifai.db.entities.data.DataEntity;
import ai.classifai.db.entities.dataVersion.DataVersionEntity;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AnnotationEntity implements Annotation {
    public static final String ANNOTATION_ID_KEY = "annotation_id";
    public static final String COLOR_KEY = "color";
    public static final String LABEL_KEY = "label";
    public static final String LINE_WIDTH_KEY = "lineWidth";

    @Id
    private Long id;

    // commit order of annotations
    private Integer position;

    @ManyToOne
    @JoinColumn(name= LABEL_KEY)
    private LabelEntity label;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = DataEntity.DATA_ID_KEY, referencedColumnName = DataEntity.DATA_ID_KEY),
            @JoinColumn(name = VersionEntity.VERSION_ID_KEY, referencedColumnName = VersionEntity.VERSION_ID_KEY)
    })
    private DataVersionEntity dataVersionEntity;
}
