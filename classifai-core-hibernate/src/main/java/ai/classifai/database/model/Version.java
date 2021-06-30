package ai.classifai.database.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "VERSION")
public class Version
{
    public static final String VERSION_ID_KEY = "version_id";
    public static final String CREATED_DATE_KEY = "created_date";
    public static final String LAST_MODIFIED_DATE_KEY = "last_modified_date";

    @Id
    @GeneratedValue
    @Column(name = VERSION_ID_KEY)
    private UUID versionId;

    @Column(name = CREATED_DATE_KEY)
    private Date createdDate;

    @Column(name = LAST_MODIFIED_DATE_KEY)
    private Date lastModifiedDate;

    @ManyToOne
    @JoinColumn(name=Project.PROJECT_ID_KEY, nullable = false)
    private Project project;

    @ManyToMany(mappedBy = "versionList")
    private List<Label> labelList;

    public Version() {
        createdDate = new Date();
        lastModifiedDate = new Date();
    }

    public void updateLastModifiedDate()
    {
        lastModifiedDate = new Date();
    }
}
