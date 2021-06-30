package ai.classifai.database.model.annotation;

import ai.classifai.database.model.Version;
import ai.classifai.database.model.data.Data;
import ai.classifai.database.model.versiondata.DataVersion;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Annotation
{
    @Id
    private Long annotationId;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "data_id", referencedColumnName = "data_id"),
            @JoinColumn(name = "version_id", referencedColumnName = "version_id")
    })
    private DataVersion dataVersion;
}
