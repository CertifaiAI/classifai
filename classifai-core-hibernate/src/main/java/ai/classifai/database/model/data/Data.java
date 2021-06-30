package ai.classifai.database.model.data;


import ai.classifai.database.model.Project;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.UUID;

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

    public Data(String dataPath, String checksum, long fileSize)
    {
        this.dataPath = dataPath;
        this.checksum = checksum;
        this.fileSize = fileSize;
    }

    public Data()
    {
    }
}
