package ai.classifai.database.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "VERSION")
public class Version
{
    @Id
    @Column(name = "version_id")
    private UUID versionId;

    @Column(name = "created_date")
    private Date createdDate;

    @ManyToOne
    @Column(name = "project_id")
    private Project project;

    @ManyToMany
    private List<Label> labelList;
}
