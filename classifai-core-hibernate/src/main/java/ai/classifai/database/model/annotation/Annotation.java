package ai.classifai.database.model.annotation;

import ai.classifai.database.model.Model;
import ai.classifai.database.model.dataVersion.DataVersion;

import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Annotation implements Model
{
    public static final String ANNOTATION_ID_KEY = "annotation_id";

    @Id
    @Column(name = ANNOTATION_ID_KEY)
    private Long annotationId;

    @ManyToOne
    @JoinColumn(name = DataVersion.DATA_VERSION_ID_KEY)
    private DataVersion dataVersion;

    public Annotation(Long annotationId)
    {
        this.annotationId = annotationId;
    }

    public Annotation() {}

    @Override
    public boolean isPersisted() {
        return annotationId != null;
    }
}
