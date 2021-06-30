package ai.classifai.database.model.versiondata;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
public class DataVersionKey implements Serializable
{
    @Column(name = "data_id")
    private UUID dataId;

    @Column(name = "version_id")
    private UUID versionId;
}
