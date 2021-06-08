package ai.classifai.database.model.data;

import ai.classifai.database.model.Project;
import ai.classifai.database.model.annotation.Annotation;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
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
    private int fileSize;

    @ManyToOne
    @Column(name = "project_id")
    private Project project;
}
