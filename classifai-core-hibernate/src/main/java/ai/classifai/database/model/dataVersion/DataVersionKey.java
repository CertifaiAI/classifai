package ai.classifai.database.model.dataVersion;

import ai.classifai.database.model.Version;
import ai.classifai.database.model.data.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Setter
@Getter
@Embeddable
public class DataVersionKey implements Serializable
{
    @Serial
    private static final long serialVersionUID = 2061522709097353044L;

    @Column(name = Data.DATA_ID_KEY)
    private UUID dataId;

    @Column(name = Version.VERSION_ID_KEY)
    private UUID versionId;

    public DataVersionKey() {}

    public DataVersionKey(UUID dataId, UUID versionId)
    {
        this.dataId = dataId;
        this.versionId = versionId;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof DataVersionKey)
        {
            DataVersionKey dataVersionKey = (DataVersionKey) obj;
            return dataId.equals(dataVersionKey.getDataId()) &&
                    versionId.equals(dataVersionKey.getVersionId());
        }
        return false;
    }
}
