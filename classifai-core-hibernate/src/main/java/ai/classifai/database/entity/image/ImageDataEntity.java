package ai.classifai.database.entity.image;

import ai.classifai.core.entity.dto.generic.DataDTO;
import ai.classifai.core.entity.dto.image.ImageDataDTO;
import ai.classifai.core.entity.model.image.ImageData;
import ai.classifai.database.entity.generic.DataEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * Class for ImageData entity with hibernate implementation
 *
 * @author YinChuangSum
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity(name = "image_data")
public class ImageDataEntity extends DataEntity implements ImageData
{
    @Column(name = "depth")
    private Integer depth;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    public ImageDataEntity()
    {
        super();
    }

    @Override
    public ImageDataDTO toDTO()
    {
        return ImageDataDTO.builder()
                .id(getId())
                .checksum(getChecksum())
                .path(getPath())
                .fileSize(getFileSize())
                .projectId(getProject().getId())
                .depth(depth)
                .width(width)
                .height(height)
                .build();
    }

    @Override
    public void fromDTO(DataDTO dataDTO)
    {
        ImageDataDTO dto = ImageDataDTO.toDTOImpl(dataDTO);
        setChecksum(dto.getChecksum());
        setFileSize(dto.getFileSize());
        setDepth(dto.getDepth());
        setWidth(dto.getWidth());
        setHeight(dto.getHeight());
        update(dto);
    }

    @Override
    public void update(DataDTO dataDTO)
    {
        ImageDataDTO dto = ImageDataDTO.toDTOImpl(dataDTO);
        setPath(dto.getPath());
    }

}
