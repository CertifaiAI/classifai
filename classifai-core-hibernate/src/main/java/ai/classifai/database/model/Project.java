package ai.classifai.database.model;

import ai.classifai.database.model.data.Data;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@Entity
public class Project
{
    @Id
    @GeneratedValue
    private UUID projectId;

    @NotNull
    private String projectName;

    @NotNull
    private int annoType;

    @NotNull
    private String projectPath;

    @NotNull
    private boolean isNew;

    @NotNull
    private boolean isStarred;

    @NotNull
    private int projectInfra;

    @OneToMany
    private List<Data> dataList;
}
