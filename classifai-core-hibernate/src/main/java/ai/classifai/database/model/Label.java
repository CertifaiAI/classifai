package ai.classifai.database.model;

import jakarta.validation.constraints.NotNull;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
public class Label
{
    public Label(String value)
    {
        this.value = value;
    }

    @Id
    @NotNull
    private String value;

    @ManyToMany
    private List<Version> versionList;

    public static List<Label> fromStringList(List<String> list)
    {
        return list.stream()
                .map(Label::new)
                .collect(Collectors.toList());
    }
}
