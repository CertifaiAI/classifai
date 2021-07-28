package ai.classifai.database.entity.generic;

import ai.classifai.core.entity.dto.generic.LabelDTO;
import ai.classifai.core.entity.model.generic.Label;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@NoArgsConstructor
@lombok.Data
@Entity(name = "label")
public class LabelEntity implements Label
{
    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "color")
    private String color;

    @ManyToOne
    @JoinColumn(name = "version_id")
    private VersionEntity version;

    public LabelDTO toDTO()
    {
        return LabelDTO.builder()
                .id(id)
                .name(name)
                .versionId(version.getId())
                .color(color)
                .build();
    }

    @Override
    public void fromDTO(LabelDTO dto)
    {
        setId(dto.getId());
        update(dto);
    }

    @Override
    public void update(LabelDTO dto)
    {
        setColor(dto.getColor());
        setName(dto.getName());
    }
}
