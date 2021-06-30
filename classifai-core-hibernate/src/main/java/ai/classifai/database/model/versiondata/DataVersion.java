package ai.classifai.database.model.versiondata;

import ai.classifai.database.model.Version;
import ai.classifai.database.model.annotation.Annotation;
import ai.classifai.database.model.data.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "DATA_VERSION")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class DataVersion
{
    @EmbeddedId
    private DataVersionKey dataVersionKey;

    @ManyToOne
    @MapsId("dataId")
    @JoinColumn(name = "data_id")
    private Data data;

    @ManyToOne
    @MapsId("versionId")
    @JoinColumn(name = "version_id")
    private Version version;

    @OneToMany(mappedBy = "dataVersion")
    private List<Annotation> annotation;
}
