package ai.classifai.database.model.data;

import ai.classifai.database.model.Project;
import ai.classifai.database.model.annotation.Annotation;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.UUID;

@Getter
@Setter
@Entity
public abstract class Data
{
    @Id
    @GeneratedValue
    private UUID dataId;

    @ManyToOne
    private Project project;
}
