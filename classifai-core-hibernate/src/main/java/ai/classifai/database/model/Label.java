package ai.classifai.database.model;

import jakarta.validation.constraints.NotNull;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
public class Label
{
    @Id
    @NotNull
    @Column(name = "label")
    private String value;

    @ManyToMany
    @JoinTable(
            name = "label_version",
            joinColumns = @JoinColumn(name = "label"),
            inverseJoinColumns = @JoinColumn(name = "version_id")
    )
    private List<Version> versionList;

    public Label(String value)
    {
        this.value = value;
    }

    public static List<Label> fromStringList(List<String> list)
    {
        return list.stream()
                .map(Label::new)
                .collect(Collectors.toList());
    }
}
