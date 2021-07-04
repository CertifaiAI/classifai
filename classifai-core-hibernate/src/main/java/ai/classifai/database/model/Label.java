package ai.classifai.database.model;

import ai.classifai.database.model.data.Data;
import jakarta.validation.constraints.NotNull;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
public class Label implements Model
{
    public static final String LABEL_ID_KEY = "label_id";
    public static final String VALUE_KEY = "value";
    public static final String LABEL_VERSION_TABLE_NAME = "label_version";

    @Id
    @GeneratedValue
    @Column(name = LABEL_ID_KEY)
    private UUID labelId;

    @Column(name = VALUE_KEY)
    private String value;

    @ManyToOne
    @JoinColumn(name = Version.VERSION_ID_KEY, nullable = false)
    private Version version;

    public Label(String value, Version version)
    {
        this.value = value;
        this.version = version;
    }

    public Label() {}

    public static List<Label> labelListFromStringList(List<String> list, Version version)
    {
        return list.stream()
                .map(string -> new Label(string, version))
                .collect(Collectors.toList());
    }

    public static List<Label> getLabelListFromVersionList(List<Version> versionList)
    {
        return versionList.stream()
                .map(Version::getLabelList)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object obj) {
        Label label = (Label) obj;
        return value.equals(label.value);
    }

    @Override
    public boolean isPersisted() {
        return labelId != null;
    }
}
