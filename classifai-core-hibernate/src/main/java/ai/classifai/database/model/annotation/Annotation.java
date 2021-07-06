package ai.classifai.database.model.annotation;

import ai.classifai.database.model.Model;
import ai.classifai.database.model.dataVersion.DataVersion;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Annotation implements Model
{
    @Setter
    @Getter
    @Embeddable
    public static class AnnotationKey implements Serializable
    {
        @Serial
        private static final long serialVersionUID = -4361684840738331803L;

        @Column(name = ANNOTATION_ID_KEY)
        private long annotationId;

        @Column(name = DataVersion.DATA_VERSION_ID_KEY)
        private UUID dataVersionId;

        public AnnotationKey() {}

        public AnnotationKey(long annotationId, UUID dataVersionId)
        {
            this.annotationId = annotationId;
            this.dataVersionId = dataVersionId;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof AnnotationKey)
            {
                AnnotationKey annoKey = (AnnotationKey) obj;
                return annotationId == annoKey.getAnnotationId() &&
                        dataVersionId.equals(annoKey.getDataVersionId());
            }
            return false;
        }
    }

    public static final String ANNOTATION_ID_KEY = "annotation_id";

    @EmbeddedId
    private AnnotationKey annotationKey;

    // persist order of annotations
    @Column(name = "position")
    private int position;

    @ManyToOne
    @MapsId("dataVersionId")
    @JoinColumn(name = DataVersion.DATA_VERSION_ID_KEY)
    private DataVersion dataVersion;

    public Annotation(long annotationId, DataVersion dataVersion, int position)
    {
        this.dataVersion = dataVersion;
        this.annotationKey = new AnnotationKey(annotationId, dataVersion.getDataVersionId());
        this.position = position;
    }

    public Annotation() {}

    @Override
    // no way to determine it is persisted or not, always assume it is persisted
    public boolean isPersisted() {
        return true;
    }

    public abstract JsonObject outputJson();

    public static List<JsonObject> getAnnotationJsonList(List<Annotation> annotationList)
    {
        return annotationList.stream()
                .sorted(Comparator.comparingInt(a -> a.position))
                .map(Annotation::outputJson)
                .collect(Collectors.toList());
    }
}
