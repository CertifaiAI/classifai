package ai.classifai.util.data;

import ai.classifai.db.entities.ProjectEntity;
import ai.classifai.db.entities.data.DataEntity;
import ai.classifai.util.exception.NotSupportedDataTypeException;
import ai.classifai.util.type.AnnotationType;
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

    public abstract List<DataEntity> getDataList(ProjectEntity project);

    public abstract List<DataEntity> getNewlyAddedDataList(ProjectEntity project);

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

    public abstract String generateDataSource(DataEntity dataEntity);

    public static List<String> getDataIdList(List<DataEntity> dataEntityList)
    {
        return dataEntityList.stream()
                .map(DataEntity::getDataId)
                .map(UUID::toString)
                .collect(Collectors.toList());
    }
}
