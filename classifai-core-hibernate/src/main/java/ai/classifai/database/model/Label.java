package ai.classifai.database.model;

import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@NoArgsConstructor
@Entity
public class Label implements Model
{
    public static final String LABEL_ID_KEY = "label_id";
    public static final String VALUE_KEY = "value";
    public static final String COLOR_KEY = "color";


    @Id
    @GeneratedValue
    @Column(name = LABEL_ID_KEY)
    private UUID labelId;

    @Column(name = VALUE_KEY)
    private String value;

    @Column(name = COLOR_KEY)
    private String color;

    @ManyToOne
    @JoinColumn(name = Version.VERSION_ID_KEY, nullable = false)
    private Version version;

    public Label(String value, Version version)
    {
        this.value = value;
        this.version = version;
        // FIXME: hardcoded
        color = "rgba(255,255,0,0.8)";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Label)
        {
            Label label = (Label) obj;
            return value.equals(label.value);
        }
        return false;
    }

    @Override
    public boolean isPersisted() {
        return labelId != null;
    }

    @Override
    public String toString() {
        return value;
    }
}
