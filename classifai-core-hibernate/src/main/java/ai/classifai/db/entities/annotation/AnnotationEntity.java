package ai.classifai.db.entities.annotation;

import ai.classifai.core.entities.Annotation;
import ai.classifai.db.entities.VersionEntity;
import ai.classifai.db.entities.data.DataEntity;
import ai.classifai.db.entities.dataVersion.DataVersionEntity;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AnnotationEntity implements Annotation
{
    public static final String ANNOTATION_ID_KEY = "annotation_id";
    public static final String COLOR_KEY = "color";
    public static final String LABEL_KEY = "label";
    public static final String LINE_WIDTH_KEY = "lineWidth";

    @EmbeddedId
    private AnnotationKey annotationKey;

    // persist order of annotations
    @Column(name = "position")
    private int position;

    @Column(name = COLOR_KEY)
    private String color;

    @Column(name = LABEL_KEY)
    private String label;

    @Column(name = LINE_WIDTH_KEY)
    private int lineWidth;

    @ManyToOne
    @MapsId("dataVersionKey")
    @JoinColumns({
            @JoinColumn(name = DataEntity.DATA_ID_KEY, referencedColumnName = DataEntity.DATA_ID_KEY),
            @JoinColumn(name = VersionEntity.VERSION_ID_KEY, referencedColumnName = VersionEntity.VERSION_ID_KEY)
    })
    private DataVersionEntity dataVersionEntity;

    public AnnotationEntity(long annotationId, DataVersionEntity dataVersionEntity, String color, String label, int lineWidth, int position)
    {
        this.dataVersionEntity = dataVersionEntity;
        this.annotationKey = new AnnotationKey(annotationId, dataVersionEntity.getDataVersionKey());
        this.color = color;
        this.label = label;
        this.lineWidth = lineWidth;
        this.position = position;
    }

    public AnnotationEntity() {}

    @Override
    // no way to determine it is persisted or not, always assume it is persisted
    public boolean isPersisted() {
        return true;
    }

    public abstract JsonObject outputJson();

    public static List<JsonObject> getAnnotationJsonList(List<AnnotationEntity> annotationEntityList)
    {
        return annotationEntityList.stream()
                .sorted(Comparator.comparingInt(a -> a.position))
                .map(AnnotationEntity::outputJson)
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AnnotationEntity)
        {
            AnnotationEntity annotationEntity = (AnnotationEntity) obj;
            return annotationKey.equals(((AnnotationEntity) obj).getAnnotationKey());
        }
        return false;
    }
}
