package ai.classifai.database.model.dataVersion;

import ai.classifai.database.model.Version;
import ai.classifai.database.model.annotation.Annotation;
import ai.classifai.database.model.data.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "DATA_VERSION")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class DataVersion
{
    @Embeddable
    public static class DataVersionKey implements Serializable
    {
        @Serial
        private static final long serialVersionUID = 8036480971883487344L;

        private UUID dataId;

        private UUID versionId;

        public DataVersionKey(Data data, Version version)
        {
            dataId = data.getDataId();

            versionId = version.getVersionId();
        }

        public DataVersionKey() {}
    }

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
    private List<Annotation> annotations;

    public DataVersion(Data data, Version version)
    {
        annotations = new ArrayList<>();

        this.data = data;
        this.version = version;
        this.dataVersionKey = new DataVersionKey(data, version);
    }

    public DataVersion()
    {}

    public Boolean isVersion(Version version)
    {
        return this.version.equals(version);
    }

    public void addAnnotation()
    {

    }
}
