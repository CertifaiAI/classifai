package ai.classifai.database.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "VERSION")
public class Version
{
    @Id
    private UUID versionId;

    @ManyToOne
    private Project project;

    @OneToMany
    private List<Label> labelList;
}
