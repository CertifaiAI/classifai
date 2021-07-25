package ai.classifai.database.entity.generic;

import ai.classifai.core.entity.dto.generic.AnnotationDTO;
import ai.classifai.core.entity.model.generic.Annotation;
import ai.classifai.database.entity.generic.LabelEntity;
import ai.classifai.database.entity.generic.DataVersionEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity(name = "annotation")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AnnotationEntity implements Annotation
{
    @Id
    @Column(name = "id")
    private Long id;

    // persist order of annotations
    @Column(name = "position")
    private Integer position;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "label_id")
    private LabelEntity label;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumns({
            @JoinColumn(name = "data_id", referencedColumnName = "data_id"),
            @JoinColumn(name = "version_id", referencedColumnName = "version_id")
    })
    private DataVersionEntity dataVersion;
}
