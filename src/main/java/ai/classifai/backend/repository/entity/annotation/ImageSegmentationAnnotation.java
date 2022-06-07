package ai.classifai.backend.repository.entity.annotation;

import ai.classifai.core.services.annotation.AnnotationAbstract;
import ai.classifai.backend.dto.SegmentationDTO;
import ai.classifai.backend.dto.properties.ImageProperties;
import ai.classifai.backend.dto.properties.SegmentationProperties;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ImageSegmentationAnnotation implements AnnotationAbstract<SegmentationDTO, SegmentationProperties> {
    private ImageProperties imageProperties;
    private List<SegmentationProperties> segmentationPropertiesList;
    @Getter
    private SegmentationDTO segmentationDTO;

    public ImageSegmentationAnnotation(ImageProperties imageProperties) {
        this.imageProperties = imageProperties;
    }

    @Override
    public SegmentationDTO createAnnotation(SegmentationProperties segmentationProperties) {
        this.segmentationPropertiesList.add(segmentationProperties);
        return toDTO(segmentationProperties);
    }

    @Override
    public SegmentationDTO getAnnotationById(String uuid) {
        SegmentationProperties segmentationPropertiesById = segmentationPropertiesList.stream()
                .filter(segmentationProperties -> segmentationProperties.getUuid().equals(uuid))
                .findFirst()
                .orElse(null);
        return toDTO(segmentationPropertiesById);
    }

    @Override
    public void deleteAnnotationById(String id) {
        this.segmentationPropertiesList.removeIf(segmentationProperties -> segmentationProperties.getUuid().equals(id));
    }

    @Override
    public List<SegmentationDTO> listAnnotations() {
        List<SegmentationDTO> segmentationDTOList = new ArrayList<>();
        for (SegmentationProperties properties : segmentationPropertiesList) {
            segmentationDTOList.add(toDTO(properties));
        }
        return segmentationDTOList;
    }

    @Override
    public SegmentationDTO toDTO(SegmentationProperties annotation) {
        this.segmentationDTO = SegmentationDTO.builder()
                .imgPath(imageProperties.getImgUuid())
                .imgPath(imageProperties.getImgPath())
                .imgOriginalWidth(imageProperties.getImgOriginalWidth())
                .imgOriginalHeight(imageProperties.getImgOriginalHeight())
                .imgX(imageProperties.getImgX())
                .imgY(imageProperties.getImgY())
                .imgW(imageProperties.getImgW())
                .imgH(imageProperties.getImgH())
                .segmentationPropertiesList(this.segmentationPropertiesList)
                .fileSize(imageProperties.getFileSize())
                .imgThumbnail(imageProperties.getImageThumbnail())
                .build();

        return segmentationDTO;
    }
}
