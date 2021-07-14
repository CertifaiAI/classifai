package ai.classifai.entities.dto;

import java.io.Serial;
import java.io.Serializable;

public abstract class ExportDTO<T extends ExportDTO<T>> implements Serializable {
    @Serial
    private static final long serialVersionUID = 2223075402846956189L;

    public abstract T readJson(String jsonString);
    public abstract String toJson();
}
