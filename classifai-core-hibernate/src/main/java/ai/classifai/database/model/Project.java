package ai.classifai.database.model;

import ai.classifai.action.LabelListImport;
import ai.classifai.database.model.data.Data;
import ai.classifai.util.data.DataHandler;
import ai.classifai.util.data.LabelHandler;
import ai.classifai.util.exception.NotSupportedDataTypeException;
import ai.classifai.util.project.ProjectInfra;
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
    public static final String ANNO_TYPE_KEY = "anno_type";
    public static final String PROJECT_PATH_KEY = "project_path";
    public static final String IS_NEW_KEY = "is_new";
    public static final String IS_STARRED_KEY = "is_starred";
    public static final String PROJECT_INFRA_KEY = "project_infra";
    public static final String CURRENT_VERSION_KEY = "current_version";

    @Id
    @GeneratedValue
    @Column(name = PROJECT_ID_KEY)
    private UUID projectId;

    @Column(name = PROJECT_NAME_KEY)
    private String projectName;

    @Column(name = ANNO_TYPE_KEY)
    private int annoType;

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

    // TODO: refer using reflection? -> will get compilation error instead of runtime error
    // field name in Data class
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<Data> dataList;

    // field name in Version class
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<Version> versionList;

    public Project()
    {}

    public Project(String projectName, int annoType, String projectPath,
                   boolean isNew, boolean isStarred,int projectInfra)
    {
        this.dataList = new ArrayList<>();
        this.versionList = new ArrayList<>();
        this.currentVersion = addNewVersion();
        this.projectName = projectName;
        this.annoType = annoType;
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
        jsonObj.put("is_loaded", false); //TODO: hardcoded implement method
        jsonObj.put("is_cloud", false); //TODO: hardcoded implement method
        jsonObj.put(PROJECT_INFRA_KEY, projectInfra);
        jsonObj.put(Version.CREATED_DATE_KEY, currentVersion.getCreatedDate().toString());
        jsonObj.put(Version.LAST_MODIFIED_DATE_KEY, currentVersion.getLastModifiedDate().toString());
        jsonObj.put(CURRENT_VERSION_KEY, currentVersion.getVersionId().toString());
        jsonObj.put("total_uuid", dataList.size());
        jsonObj.put("root_path_valid", isRootPathValid());

        return jsonObj;
    }

    // create new project
    public static Project buildNewProject(String projectName, int annoType, String projectPath, String labelPath)
            throws NotSupportedDataTypeException
    {
        Project project = new Project(projectName, annoType, projectPath, true,
                false, ProjectInfra.ON_PREMISE.ordinal());

        // create new Data
        DataHandler dataHandler = DataHandler.getDataHandler(annoType);
        List<Data> dataList = dataHandler.getDataList(project);

        dataList.forEach(project::addData);

        // process label path get label list
        LabelHandler labelHandler = new LabelHandler();
        List<String> strLabelList = new LabelListImport(new File(labelPath)).getValidLabelList();
        List<Label> labelList = labelHandler.getLabelList(project, strLabelList);

        labelList.forEach(project.currentVersion::addLabel);

        return project;
    }

    @Override
    public boolean isPersisted() {
        return projectId != null;
    }
}
