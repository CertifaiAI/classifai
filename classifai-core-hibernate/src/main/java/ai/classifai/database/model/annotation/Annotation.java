package ai.classifai.database.model.annotation;

import ai.classifai.database.model.Version;
import ai.classifai.database.model.data.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.UUID;

@Entity
public abstract class Annotation
{
    @Id
    private UUID annotationId;

    @ManyToOne
    private Data Data;

    @ManyToOne
    private Version version;
}
