package ai.classifai.db.entities.dataVersion;

import ai.classifai.core.entities.DataVersion;
import ai.classifai.core.entities.dto.dataversion.DataVersionDTO;
import ai.classifai.db.entities.VersionEntity;
import ai.classifai.db.entities.annotation.AnnotationEntity;
import ai.classifai.db.entities.data.DataEntity;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "DATA_VERSION")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class DataVersionEntity implements DataVersion
{
    public static final String DATA_VERSION_ID_KEY = "data_version_id";

    @EmbeddedId
    private DataVersionKey id;

    @ManyToOne
    @MapsId(value = "dataId")
    @JoinColumn(name = DataEntity.DATA_ID_KEY)
    private DataEntity dataEntity;

    @ManyToOne
    @MapsId(value = "versionId")
    @JoinColumn(name = VersionEntity.VERSION_ID_KEY)
    private VersionEntity versionEntity;

    @Getter
    @OneToMany(mappedBy = "dataVersion")
    private List<AnnotationEntity> annotationList;

    @Override
    public DataVersionDTO toDTO() {
        return null;
    }
}
