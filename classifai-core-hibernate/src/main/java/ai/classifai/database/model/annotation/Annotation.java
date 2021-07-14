package ai.classifai.database.model.annotation;

import ai.classifai.database.model.Model;
import ai.classifai.database.model.Version;
import ai.classifai.database.model.data.Data;
import ai.classifai.database.model.dataVersion.DataVersion;
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
public abstract class Annotation implements Model
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
            @JoinColumn(name = Data.DATA_ID_KEY, referencedColumnName = Data.DATA_ID_KEY),
            @JoinColumn(name = Version.VERSION_ID_KEY, referencedColumnName = Version.VERSION_ID_KEY)
    })
    private DataVersion dataVersion;

    public Annotation(long annotationId, DataVersion dataVersion, String color, String label, int lineWidth, int position)
    {
        this.dataVersion = dataVersion;
        this.annotationKey = new AnnotationKey(annotationId, dataVersion.getDataVersionKey());
        this.color = color;
        this.label = label;
        this.lineWidth = lineWidth;
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Annotation)
        {
            Annotation annotation = (Annotation) obj;
            return annotationKey.equals(((Annotation) obj).getAnnotationKey());
        }
        return false;
    }
}
