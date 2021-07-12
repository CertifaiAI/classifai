package ai.classifai.database.model.annotation;

import ai.classifai.database.model.dataVersion.DataVersion;
import ai.classifai.database.model.dataVersion.DataVersionKey;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Columns;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Embeddable
public class AnnotationKey implements Serializable
{
    public static final String ANNOTATION_ID_KEY = "annotation_id";

    @Serial
    private static final long serialVersionUID = -4361684840738331803L;

    @Column(name = ANNOTATION_ID_KEY)
    private long annotationId;

    private DataVersionKey dataVersionKey;

    public AnnotationKey() {}

    public AnnotationKey(long annotationId, DataVersionKey dataVersionKey)
    {
        this.annotationId = annotationId;
        this.dataVersionKey = dataVersionKey;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof AnnotationKey)
        {
            AnnotationKey annoKey = (AnnotationKey) obj;
            return annotationId == annoKey.getAnnotationId() &&
                    dataVersionKey.equals(annoKey.getDataVersionKey());
        }
        return false;
    }
}
