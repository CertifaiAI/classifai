package ai.classifai.database.model.annotation;

import ai.classifai.database.model.dataVersion.DataVersion;

import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Annotation
{
    @Id
    @Column(name = "annotation_id")
    private Long annotationId;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "data_id", referencedColumnName = "data_id"),
            @JoinColumn(name = "version_id", referencedColumnName = "version_id")
    })
    private DataVersion dataVersion;

    public Annotation(Long annotationId)
    {
        this.annotationId = annotationId;
    }

    public Annotation() {}
}
