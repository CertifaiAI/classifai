package ai.classifai.database.model;

import jakarta.validation.constraints.NotNull;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.UUID;

@Entity
public class Label
{
    @Id
    private UUID labelId;

    @NotNull
    private String value;

    @ManyToOne
    private Version version;
}
