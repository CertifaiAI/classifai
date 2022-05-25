package ai.classifai.repository.annotation;

import java.util.Map;

public class ImageSegmentationAnnotation implements AnnotationType<Segmentation>{
    private String imgUuid;
    private String imgPath;
    private Integer imgDepth;
    private Integer imgOriW;
    private Integer imgOriH;
    private Integer fileSize;
    private Map<String, Segmentation> segmentationMap;

    ImageSegmentationAnnotation(String imgUuid, String imgPath, Integer imgDepth,
                                Integer imgOriH, Integer imgOriW, Integer fileSize) {
        this.imgUuid = imgUuid;
        this.imgPath = imgPath;
        this.imgDepth = imgDepth;
        this.imgOriH = imgOriH;
        this.imgOriW = imgOriW;
        this.fileSize = fileSize;
    }

    @Override
    public Segmentation createAnnotation(Segmentation segmentation) {
        return segmentationMap.put(segmentation.getUuid(), segmentation);
    }

    @Override
    public Segmentation getAnnotationById(String annotationUuid) {
        return this.segmentationMap.get(annotationUuid);
    }

    @Override
    public void deleteAnnotationById(String id) {

    }

    @Override
    public void toDTO(Segmentation annotation) {

    }
}
