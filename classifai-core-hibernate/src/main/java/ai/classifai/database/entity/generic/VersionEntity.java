package ai.classifai.database.entity.generic;

import ai.classifai.core.entity.dto.generic.VersionDTO;
import ai.classifai.core.entity.model.generic.Data;
import ai.classifai.core.entity.model.generic.Label;
import ai.classifai.core.entity.model.generic.Version;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@lombok.Data
@Entity(name = "version")
public class VersionEntity implements Version
{
    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @OneToMany(mappedBy = "version",
            cascade = CascadeType.ALL)
    private List<LabelEntity> labelList;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="project_id")
    private ProjectEntity project;

    @OneToMany(mappedBy = "version",
            cascade = CascadeType.ALL)
    private List<DataVersionEntity> dataVersionList;

    public VersionEntity()
    {
        labelList = new ArrayList<>();
        dataVersionList = new ArrayList<>();
    }

    @Override
    public List<Label> getLabelList()
    {
        return new ArrayList<>(labelList);
    }

    @Override
    public VersionDTO toDTO()
    {
        return VersionDTO.builder()
                .id(id)
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .labelIdList(labelList.stream()
                        .map(Label::getId)
                        .collect(Collectors.toList()))
                .projectId(project.getId())
                .build();
    }

    @Override
    public void fromDTO(VersionDTO dto)
    {
        update(dto);
    }

    @Override
    public void update(VersionDTO dto)
    {
        setCreatedAt(dto.getCreatedAt());
        setModifiedAt(dto.getModifiedAt());
    }

    public void addLabel(LabelEntity label)
    {
        label.setVersion(this);
        labelList.add(label);
    }

    public void addDataVersion(DataVersionEntity dataVersion)
    {
        dataVersion.setVersion(this);
        dataVersionList.add(dataVersion);
    }
}
