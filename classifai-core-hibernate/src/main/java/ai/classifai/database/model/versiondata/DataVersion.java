package ai.classifai.database.model.versiondata;

import ai.classifai.database.model.annotation.Annotation;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public abstract class DataVersion
{
    @EmbeddedId
    private DataVersionKey id;

    @OneToMany
    private List<Annotation> annotation;
}
