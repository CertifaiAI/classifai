package ai.classifai.repository.annotation;

import ai.classifai.dto.SegmentationDTO;
import ai.classifai.dto.properties.ImageProperties;
import ai.classifai.dto.properties.Segmentation;
import lombok.Getter;

import java.util.List;

public class ImageSegmentationAnnotation extends AnnotationAbstract<SegmentationDTO, Segmentation> implements AnnotationType<Segmentation>{
    private ImageProperties imageProperties;
    private List<Segmentation> segmentationList;
    @Getter
    private SegmentationDTO segmentationDTO;

    ImageSegmentationAnnotation(ImageProperties imageProperties) {
        this.imageProperties = imageProperties;
    }

    @Override
    public Segmentation createAnnotation(Segmentation segmentation) {
        this.segmentationList.add(segmentation);
        return segmentation;
    }

    @Override
    public Segmentation getAnnotationById(String uuid) {
        Segmentation segmentationById = segmentationList.stream()
                .filter(segmentation -> segmentation.getUuid().equals(uuid))
                .findFirst()
                .orElse(null);
        return segmentationById;
    }

    @Override
    public void deleteAnnotationById(String id) {
        this.segmentationList.removeIf(segmentation -> segmentation.getUuid().equals(id));
    }

    @Override
    public SegmentationDTO toDTO(Segmentation annotation) {
        this.segmentationDTO = SegmentationDTO.builder()
                .imgPath(imageProperties.getImgUuid())
                .imgPath(imageProperties.getImgPath())
                .imgOriginalWidth(imageProperties.getImgOriginalWidth())
                .imgOriginalHeight(imageProperties.getImgOriginalHeight())
                .imgX(imageProperties.getImgX())
                .imgY(imageProperties.getImgY())
                .imgW(imageProperties.getImgW())
                .imgH(imageProperties.getImgH())
                .segmentationList(this.segmentationList)
                .fileSize(imageProperties.getFileSize())
                .imgThumbnail(imageProperties.getImageThumbnail())
                .build();

        return segmentationDTO;
    }
}
