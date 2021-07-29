package ai.classifai.core.entity.trait;

/**
 * Every entity interface must implement HasDTO with a DTO class
 *
 * @author YinChuangSum
 */
public interface HasDTO<DTO>
{
    DTO toDTO();

    // create a new instance => set all parameters
    void fromDTO(DTO dto);

    // update existing entity => set only certain parameters
    void update(DTO dto);
}
