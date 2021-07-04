package ai.classifai.util.data;

import ai.classifai.database.model.Project;
import ai.classifai.database.model.data.Data;
import ai.classifai.util.exception.NotSupportedDataTypeException;
import ai.classifai.util.type.AnnotationType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public abstract class DataHandler
{
    private static final DataHandlerFactory FACTORY = new DataHandlerFactory();
    protected FileHandler fileHandler = new FileHandler();

    public abstract List<Data> getDataList(Project project);

    protected DataHandler(){}

    public static DataHandler getDataHandler(int annoType)
    {
        try
        {
            return FACTORY.getDataHandler(annoType);
        } catch (NotSupportedDataTypeException e)
        {
            log.error(e.getMessage());
            return null;
        }
    }

    public static class DataHandlerFactory
    {
        private DataHandlerFactory() {}

        public DataHandler getDataHandler(Integer annoTypeIdx) throws NotSupportedDataTypeException {
            if (annoTypeIdx == AnnotationType.BOUNDINGBOX.ordinal() ||
                    annoTypeIdx == AnnotationType.SEGMENTATION.ordinal())
            {
                return new ImageHandler();
            }
            else
            {
                throw new NotSupportedDataTypeException(String.format("file type %d not supported", annoTypeIdx));
            }
        }
    }
}
