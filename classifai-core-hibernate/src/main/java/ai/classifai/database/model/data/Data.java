package ai.classifai.database.model.data;


import ai.classifai.database.model.Project;
import ai.classifai.database.model.dataVersion.DataVersion;
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
public abstract class Data
{
    @Id
    @GeneratedValue
    @Column(name = "data_id")
    private UUID dataId;

    @Column(name = "data_path")
    private String dataPath;

    @Column(name = "checksum")
    private String checksum;

    @Column(name = "file_size")
    private long fileSize;

    @ManyToOne
    @JoinColumn(name="project_id", nullable = false)
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

    public DataVersion getCurrentDataVersion()
    {
        Optional<DataVersion> dataVersion = dataVersions.stream()
                .filter(dv -> dv.isVersion(this.getProject().getCurrentVersion()))
                .findFirst();

        if (dataVersion.isEmpty())
        {
            log.error("No data_version found! This should be created when every version & data is created");
            return null;
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

    public static List<DataVersion> getDataVersionListFromDataList(List<Data> dataList)
    {
        return dataList.stream()
                .map(Data::getDataVersions)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}
