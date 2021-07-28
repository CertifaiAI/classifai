package ai.classifai.database.entity.generic;

import ai.classifai.core.entity.dto.generic.DataDTO;
import ai.classifai.core.entity.model.generic.Data;
import ai.classifai.database.entity.generic.ProjectEntity;
import ai.classifai.database.entity.generic.DataVersionEntity;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.util.*;

@Slf4j
@lombok.Data
@Entity(name = "DATA")
@Inheritance(strategy = InheritanceType.JOINED)
@NamedQueries({
        @NamedQuery(name = "Data.listByProject",
        query = "select d from DATA d where d.project = :project")
})
public abstract class DataEntity implements Data
{
    public static final String listByProjectQuery = "select d from DATA d where d.project = :project";

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @Column(name = "path")
    private String path;

    @Column(name = "checksum")
    private String checksum;

    @Column(name = "file_size")
    private Long fileSize;

    @ManyToOne
    @JoinColumn(name="project_id")
    private ProjectEntity project;

    @OneToMany(mappedBy = "data",
            cascade = CascadeType.ALL)
    private List<DataVersionEntity> dataVersionList;

    public DataEntity()
    {
        dataVersionList = new ArrayList<>();
    }

    public void addDataVersion(DataVersionEntity dataVersion)
    {
        dataVersion.setData(this);
        dataVersionList.add(dataVersion);
    }
}
