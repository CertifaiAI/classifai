package ai.classifai.db.entities;

import ai.classifai.core.entities.dto.VersionDTO;
import ai.classifai.core.entities.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "VERSION")
public class VersionEntity implements Version
{
    public static final String VERSION_ID_KEY = "version_id";
    public static final String CREATED_DATE_KEY = "created_date";
    public static final String LAST_MODIFIED_DATE_KEY = "last_modified_date";

    @Id
    @GeneratedValue
    @Column(name = VERSION_ID_KEY)
    private UUID id;

    @Column(name = CREATED_DATE_KEY)
    private Instant createdAt;

    @Column(name = LAST_MODIFIED_DATE_KEY)
    private Instant modifiedAt;

    @ManyToOne
    @JoinColumn(name= ProjectEntity.PROJECT_ID_KEY)
    private ProjectEntity project;

    @OneToMany(mappedBy = "version")
    private List<LabelEntity> labelList;

    @Override
    public VersionDTO toDTO()
    {
        List<UUID> labelIds = getLabelList()
                .stream()
                .map(LabelEntity::getId)
                .collect(Collectors.toList());

        return VersionDTO.builder()
                .id(getId())
                .createdAt(getCreatedAt())
                .modifiedAt(getModifiedAt())
                .labelIds(labelIds)
                .build();
    }

    public static Version fromDTO(VersionDTO dto) {
    }
}
