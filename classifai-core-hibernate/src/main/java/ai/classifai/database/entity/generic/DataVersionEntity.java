package ai.classifai.database.entity.generic;

import ai.classifai.core.entity.dto.generic.AnnotationDTO;
import ai.classifai.core.entity.dto.generic.DataDTO;
import ai.classifai.core.entity.dto.generic.DataVersionDTO;
import ai.classifai.core.entity.model.generic.Annotation;
import ai.classifai.core.entity.model.generic.Data;
import ai.classifai.core.entity.model.generic.DataVersion;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@lombok.Data
@Entity(name = "data_version")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class DataVersionEntity implements DataVersion
{
    @EmbeddedId
    private DataVersionKey id;

    @ManyToOne(cascade = CascadeType.ALL)
    @MapsId(value = "dataId")
    @JoinColumn(name = "data_id")
    private DataEntity data;

    @ManyToOne(cascade = CascadeType.ALL)
    @MapsId(value = "versionId")
    @JoinColumn(name = "version_id")
    private VersionEntity version;

    @OneToMany(mappedBy = "dataVersion",
            cascade = CascadeType.ALL)
    private List<AnnotationEntity> annotationList;

    public DataVersionEntity()
    {
        annotationList = new ArrayList<>();
    }

    public void addAnnotation(AnnotationEntity annotation)
    {
        annotation.setDataVersion(this);
        annotationList.add(annotation);
    }

    @Override
    public List<Annotation> getAnnotations()
    {
        return new ArrayList<>(annotationList);
    }
}
