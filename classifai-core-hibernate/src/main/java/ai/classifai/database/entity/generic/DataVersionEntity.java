package ai.classifai.database.entity.generic;

import ai.classifai.core.entity.model.generic.Annotation;
import ai.classifai.core.entity.model.generic.DataVersion;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@lombok.Data
@Entity(name = "data_version")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class DataVersionEntity implements DataVersion
{
    @EmbeddedId
    private DataVersionKey id;

    @ManyToOne
    @MapsId(value = "dataId")
    @JoinColumn(name = "data_id")
    private DataEntity data;

    @ManyToOne
    @MapsId(value = "versionId")
    @JoinColumn(name = "version_id")
    private VersionEntity version;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "dataVersion")
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

    public void removeAnnotation(AnnotationEntity annotation)
    {
        annotationList.remove(annotation);
    }

    @Override
    public List<Annotation> getAnnotations()
    {
        return new ArrayList<>(annotationList);
    }
}
