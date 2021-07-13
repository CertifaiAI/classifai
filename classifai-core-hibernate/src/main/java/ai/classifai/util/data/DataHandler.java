package ai.classifai.util.data;

import ai.classifai.database.model.Project;
import ai.classifai.database.model.data.Data;
import ai.classifai.util.exception.NotSupportedDataTypeException;
import ai.classifai.util.type.AnnotationType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public abstract class DataHandler
{

    public static class DataHandlerFactory
    {
        private DataHandlerFactory() {}
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
    private static final DataHandlerFactory FACTORY = new DataHandlerFactory();
    protected FileHandler fileHandler = new FileHandler();

    public abstract List<Data> getDataList(Project project);

    public abstract List<Data> getNewlyAddedDataList(Project project);

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

    public abstract String generateDataSource(Data data);

    public static List<String> getDataIdList(List<Data> dataList)
    {
        return dataList.stream()
                .map(Data::getDataId)
                .map(UUID::toString)
                .collect(Collectors.toList());
    }
}
