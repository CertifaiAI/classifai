package ai.classifai.database.model;

import ai.classifai.database.model.data.Data;
import ai.classifai.util.project.ProjectInfra;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "PROJECT")
public class Project
{
    @Id
    @GeneratedValue
    @Setter
    @Getter
    @Column(name = "project_id")
    private UUID projectId;

    @NotNull
    @Setter
    @Getter
    @Column(name = "project_name")
    private String projectName;

    @NotNull
    @Setter
    @Getter
    @Column(name = "anno_type")
    private int annoType;

    @NotNull
    @Setter
    @Getter
    @Column(name = "project_path")
    private String projectPath;

    @NotNull
    @Setter
    @Getter
    @Column(name = "is_new")
    private boolean isNew;

    @NotNull
    @Setter
    @Getter
    @Column(name = "is_starred")
    private boolean isStarred;

    @NotNull
    @Setter
    @Getter
    @Column(name = "project_infra")
    private int projectInfra;

    @NotNull
    @OneToOne
    @Setter
    @Getter
    @Column(name = "current_version_id")
    private Version currentVersion;

    @OneToMany
    @Getter
    private List<Data> dataList;

    @NotNull
    @OneToMany
    @Setter
    @Getter
    private List<Version> versionList;

    public static Project buildProject(String projectName, int annoType)
    {
        Project project = new Project();
        List<Version> versionList = new ArrayList<>();
        Version version = new Version();
        versionList.add(version);

        project.setProjectId(UUID.randomUUID());
        project.setProjectName(projectName);
        project.setAnnoType(annoType);
        project.setCurrentVersion(version);
        project.setVersionList(versionList);
        project.setNew(true);
        project.setStarred(false);
        project.setProjectInfra(ProjectInfra.ON_PREMISE.ordinal());

        return project;
    }
}
