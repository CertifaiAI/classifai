package ai.classifai.database.model;

import ai.classifai.database.model.data.Data;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "PROJECT")
public class Project implements Model
{
    public static final String PROJECT_ID_KEY = "project_id";
    public static final String PROJECT_NAME_KEY = "project_name";
    public static final String ANNOTATION_TYPE_KEY = "annotation_type";
    public static final String PROJECT_PATH_KEY = "project_path";
    public static final String IS_NEW_KEY = "is_new";
    public static final String IS_STARRED_KEY = "is_starred";
    public static final String PROJECT_INFRA_KEY = "project_infra";
    public static final String CURRENT_VERSION_KEY = "current_version";
    public static final String IS_CLOUD_KEY = "is_cloud";
    public static final String IS_LOADED_KEY = "is_loaded";
    public static final String TOTAL_UUID_KEY = "total_uuid";
    public static final String ROOT_PATH_VALID_KEY = "root_path_valid";

    @Id
    @GeneratedValue
    @Column(name = PROJECT_ID_KEY)
    private UUID projectId;

    @Column(name = PROJECT_NAME_KEY)
    private String projectName;

    @Column(name = ANNOTATION_TYPE_KEY)
    private int annotationType;

    @Column(name = PROJECT_PATH_KEY)
    private String projectPath;

    @Column(name = IS_NEW_KEY)
    private boolean isNew;

    @Column(name = IS_STARRED_KEY)
    private boolean isStarred;

    @Column(name = PROJECT_INFRA_KEY)
    private int projectInfra;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = CURRENT_VERSION_KEY)
    private Version currentVersion;

    // field name in Data class
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<Data> dataList;

    // field name in Version class
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<Version> versionList;

    public Project()
    {}

    public Project(String projectName, int annotationType, String projectPath,
                   boolean isNew, boolean isStarred,int projectInfra)
    {
        this.dataList = new ArrayList<>();
        this.versionList = new ArrayList<>();
        this.currentVersion = addNewVersion();
        this.projectName = projectName;
        this.annotationType = annotationType;
        this.projectPath = projectPath;
        this.isNew = isNew;
        this.isStarred = isStarred;
        this.projectInfra = projectInfra;
    }

    private Boolean isRootPathValid()
    {
        return new File(projectPath).exists();
    }

    public Version addNewVersion()
    {
        Version newVersion = new Version(this);
        versionList.add(newVersion);

        return newVersion;
    }

    public void addData(Data data)
    {
        dataList.add(data);
    }

    public JsonObject getProjectMeta()
    {
        JsonObject jsonObj = new JsonObject();

        jsonObj.put(PROJECT_NAME_KEY, projectName);
        jsonObj.put(PROJECT_PATH_KEY, projectPath);
        jsonObj.put(IS_NEW_KEY, isNew);
        jsonObj.put(IS_STARRED_KEY, isStarred);
        jsonObj.put(IS_LOADED_KEY, false); //FIXME: hardcoded value
        jsonObj.put(IS_CLOUD_KEY, false); //FIXME: hardcoded value
        jsonObj.put(PROJECT_INFRA_KEY, projectInfra);
        jsonObj.put(Version.CREATED_DATE_KEY, currentVersion.getCreatedDate().toString());
        jsonObj.put(Version.LAST_MODIFIED_DATE_KEY, currentVersion.getLastModifiedDate().toString());
        jsonObj.put(CURRENT_VERSION_KEY, currentVersion.getVersionId().toString());
        jsonObj.put(TOTAL_UUID_KEY, dataList.size());
        jsonObj.put(ROOT_PATH_VALID_KEY, isRootPathValid());

        return jsonObj;
    }

    @Override
    public boolean isPersisted() {
        return projectId != null;
    }
}
