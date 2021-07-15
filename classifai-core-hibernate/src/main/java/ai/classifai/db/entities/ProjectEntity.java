package ai.classifai.db.entities;

import ai.classifai.core.entities.Data;
import ai.classifai.core.entities.Project;
import ai.classifai.core.entities.Version;
import ai.classifai.core.entities.dto.ProjectDTO;
import ai.classifai.db.entities.data.DataEntity;
import ai.classifai.util.type.AnnotationType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "PROJECT")
public class ProjectEntity implements Project
{
    public static final String PROJECT_ID_KEY = "project_id";
    public static final String PROJECT_NAME_KEY = "project_name";
    public static final String ANNOTATION_TYPE_KEY = "annotation_type";
    public static final String PROJECT_PATH_KEY = "project_path";
    public static final String IS_STARRED_KEY = "is_starred";
    public static final String PROJECT_INFRA_KEY = "project_infra";
    public static final String CURRENT_VERSION_KEY = "current_version";

    @Id
    @GeneratedValue
    @Column(name = PROJECT_ID_KEY)
    private UUID id;

    @Column(name = PROJECT_NAME_KEY)
    private String name;

    @Column(name = ANNOTATION_TYPE_KEY)
    private int annotationType;

    @Column(name = PROJECT_PATH_KEY)
    private String path;

    @Column(name = IS_STARRED_KEY)
    private boolean isStarred;

    @Column(name = PROJECT_INFRA_KEY)
    private int infra;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = CURRENT_VERSION_KEY)
    private VersionEntity currentVersion;

    // field name in Data class
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<DataEntity> dataList;

    // field name in Version class
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<VersionEntity> versionList;


    @Override
    public ProjectDTO toDTO()
    {
        UUID currentVersionId = getCurrentVersion()
                .getId();

        List<UUID> dataIds = getDataList()
                .stream()
                .map(Data::getId)
                .collect(Collectors.toList());

        List<UUID> versionIds = getVersionList()
                .stream()
                .map(Version::getId)
                .collect(Collectors.toList());

        String annotationTypeStr = AnnotationType.fromInt(getAnnotationType()).name();

        return ProjectDTO.builder()
                .id(getId())
                .name(getName())
                .annotationType(annotationTypeStr)
                .path(getPath())
                .starred(isStarred())
                .currentVersionId(currentVersionId)
                .dataIds(dataIds)
                .versionIds(versionIds)
                .build();
    }
}
