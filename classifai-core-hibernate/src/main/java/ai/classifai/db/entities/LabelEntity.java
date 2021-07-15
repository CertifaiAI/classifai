package ai.classifai.db.entities;

import ai.classifai.core.entities.Label;
import ai.classifai.core.entities.dto.LabelDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@Getter
@NoArgsConstructor
@Entity
public class LabelEntity implements Label
{
    public static final String LABEL_ID_KEY = "label_id";
    public static final String VALUE_KEY = "value";
    public static final String COLOR_KEY = "color";

    @Id
    @GeneratedValue
    @Column(name = LABEL_ID_KEY)
    private UUID id;

    @Column(name = VALUE_KEY)
    private String value;

    @Column(name = COLOR_KEY)
    private String color;

    @ManyToOne
    @JoinColumn(name = VersionEntity.VERSION_ID_KEY)
    private VersionEntity versionEntity;

    @Override
    public LabelDTO toDTO()
    {
        return LabelDTO.builder()
                .id(id)
                .value(value)
                .color(color)
                .build();
    }
}
