package ai.classifai.core.entity.trait;

public interface HasDTO<DTO>
{
    DTO toDTO();

    // create a new instance => set all parameters
    void fromDTO(DTO dto);

    // update existing entity => set only certain parameters
    void update(DTO dto);
}
