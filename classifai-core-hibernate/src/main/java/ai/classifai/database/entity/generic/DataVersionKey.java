package ai.classifai.database.entity.generic;

import ai.classifai.core.entity.model.generic.DataVersion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.Embeddable;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Composite key for DataVersion entity with hibernate implementation
 *
 * @author YinChuangSum
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class DataVersionKey implements Serializable, DataVersion.DataVersionId
{
    @Serial
    private static final long serialVersionUID = 2061522709097353044L;

    private UUID dataId;

    private UUID versionId;
}
