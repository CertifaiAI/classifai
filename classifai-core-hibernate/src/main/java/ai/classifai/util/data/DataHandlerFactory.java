package ai.classifai.util.data;

import ai.classifai.util.exception.NotSupportedDataTypeException;
import ai.classifai.util.exception.NotSupportedImageTypeException;
import ai.classifai.util.type.AnnotationType;

public class DataHandlerFactory
{
    public DataHandler getDataHandler(Integer AnnotationTypeIndex) throws NotSupportedDataTypeException {
        if (AnnotationTypeIndex == AnnotationType.BOUNDINGBOX.ordinal() ||
                AnnotationTypeIndex == AnnotationType.SEGMENTATION.ordinal())
        {
            return new ImageHandler();
        }
        else
        {
            throw new NotSupportedDataTypeException("File type not supported");
        }
    }
}
