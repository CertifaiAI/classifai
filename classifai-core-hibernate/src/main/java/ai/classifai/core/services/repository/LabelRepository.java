package ai.classifai.core.services.repository;

import ai.classifai.core.entities.Label;
import ai.classifai.core.entities.dto.LabelDTO;

import java.util.UUID;

public interface LabelRepository extends Repository<Label, LabelDTO, UUID>
{
}
