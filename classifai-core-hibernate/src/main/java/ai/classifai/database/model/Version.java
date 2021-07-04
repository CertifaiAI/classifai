package ai.classifai.database.model;

import ai.classifai.database.model.dataVersion.DataVersion;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.*;

@Getter
@Setter
@Entity
@Table(name = "VERSION")
public class Version implements Model
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

    @OneToMany(mappedBy = "version")
    protected List<DataVersion> dataVersions;

    public Version(Project project)
    {
        labelList = new ArrayList<>();
        this.project = project;
        createdDate = new Date();
        lastModifiedDate = new Date();
    }

    public Version() {}

    public void addLabel(Label label)
    {
        labelList.add(label);
    }

    public void updateLastModifiedDate()
    {
        lastModifiedDate = new Date();
    }

    public boolean equals(Version version) {
        return versionId.equals(version.getVersionId());
    }

    @Override
    public boolean isPersisted() {
        return versionId != null;
    }
}
