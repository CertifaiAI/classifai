package ai.classifai.database.model.dataVersion;

import ai.classifai.database.model.Model;
import ai.classifai.database.model.Version;
import ai.classifai.database.model.annotation.Annotation;
import ai.classifai.database.model.annotation.AnnotationListFactory;
import ai.classifai.database.model.data.Data;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
@Entity
@Table(name = "DATA_VERSION")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class DataVersion implements Model
{
    public static final String DATA_VERSION_ID_KEY = "data_version_id";

    @Id
    @GeneratedValue
    @Column(name = DATA_VERSION_ID_KEY)
    private UUID dataVersionId;

    @ManyToOne
    @JoinColumn(name = Data.DATA_ID_KEY)
    private Data data;

    @ManyToOne
    @JoinColumn(name = Version.VERSION_ID_KEY)
    private Version version;

    @Getter
    @OneToMany(mappedBy = "dataVersion")
    private List<Annotation> annotations;

    public DataVersion(Data data, Version version)
    {
        annotations = new ArrayList<>();

        this.data = data;
        this.version = version;
    }

    public DataVersion()
    {}

    @Override
    public boolean isPersisted() {
        return dataVersionId != null;
    }

    public Boolean isVersion(Version version)
    {
        return this.version.equals(version);
    }

    public static List<Annotation> getAnnotationListFromDataVersionList(List<DataVersion> dataVersionList)
    {
        return dataVersionList.stream()
                .map(DataVersion::getAnnotations)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public void updateDataFromJson(JsonObject request){
        updateDataFromJsonImplementation(request);
    }

    protected abstract void updateDataFromJsonImplementation(JsonObject request);

    public abstract JsonObject outputJson();
}
