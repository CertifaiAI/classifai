package ai.classifai.database.model.versiondata;

import ai.classifai.database.model.Version;
import ai.classifai.database.model.annotation.Annotation;
import ai.classifai.database.model.data.Data;

import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Embeddable
public class DataVersionKey implements Serializable
{
    private Data data;
    private Version version;
}
