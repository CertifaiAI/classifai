package ai.classifai.db.entities.dataVersion;

import ai.classifai.core.entities.DataVersion;
import ai.classifai.db.entities.VersionEntity;
import ai.classifai.db.entities.data.DataEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Getter
@NoArgsConstructor
@Embeddable
public class DataVersionKey implements Serializable, DataVersion.DataVersionId
{
    @Serial
    private static final long serialVersionUID = 2061522709097353044L;

    @Column(name = DataEntity.DATA_ID_KEY)
    private UUID dataId;

    @Column(name = VersionEntity.VERSION_ID_KEY)
    private UUID versionId;
}
