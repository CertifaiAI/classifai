package ai.classifai.database.model.data;


import ai.classifai.database.model.Model;
import ai.classifai.database.model.Project;
import ai.classifai.database.model.dataVersion.DataVersion;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Getter
@Setter
@Entity(name = "DATA")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Data implements Model
{
    public static final String DATA_ID_KEY = "data_id";
    public static final String DATA_PATH_KEY = "data_path";
    public static final String CHECKSUM_KEY = "checksum";
    public static final String FILE_SIZE_KEY = "file_size";

    @Id
    @GeneratedValue
    @Column(name = DATA_ID_KEY)
    private UUID dataId;

    @Column(name = DATA_PATH_KEY)
    private String dataPath;

    @Column(name = CHECKSUM_KEY)
    private String checksum;

    @Column(name = FILE_SIZE_KEY)
    private long fileSize;

    @ManyToOne
    @JoinColumn(name=Project.PROJECT_ID_KEY)
    private Project project;

    @OneToMany(mappedBy = "data")
    protected List<DataVersion> dataVersions;


    public Data(String dataPath, String checksum, long fileSize, Project project)
    {
        dataVersions = new ArrayList<>();

        this.project = project;
        this.dataPath = dataPath;
        this.checksum = checksum;
        this.fileSize = fileSize;
    }

    public Data() {}

    public abstract void addNewDataVersion();

    public abstract JsonObject loadData();

    public DataVersion getCurrentDataVersion()
    {
        Optional<DataVersion> dataVersion = dataVersions.stream()
                .filter(dv -> dv.isVersion(this.getProject().getCurrentVersion()))
                .findFirst();

        if (dataVersion.isEmpty())
        {
            String msg = "No data_version found! This should be created when every version & data is created!";
            log.error(msg);
            throw new NullPointerException(msg);
        }

        return dataVersion.get();
    }

    public boolean withId(String id)
    {
        return this.getDataId().toString().equals(id);
    }

    public String getFullPath()
    {
        return new File(this.project.getProjectPath(), this.dataPath).getAbsolutePath();
    }

    public boolean validateData()
    {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        Data data = (Data) obj;
        return (data.getDataPath().equals(dataPath) && data.getChecksum().equals(checksum));
    }

    @Override
    public boolean isPersisted() {
        return dataId != null;
    }
}
