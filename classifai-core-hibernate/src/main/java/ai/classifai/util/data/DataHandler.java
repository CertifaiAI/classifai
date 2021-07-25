package ai.classifai.util.data;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class DataHandler
{

//    public static class DataHandlerFactory
//    {
//        private DataHandlerFactory() {}
//        public DataHandler getDataHandler(Integer AnnotationTypeIndex) throws NotSupportedDataTypeException {
//            if (AnnotationTypeIndex == AnnotationType.BOUNDINGBOX.ordinal() ||
//                    AnnotationTypeIndex == AnnotationType.SEGMENTATION.ordinal())
//            {
//                return new ImageHandler();
//            }
//            else
//            {
//                throw new NotSupportedDataTypeException("File type not supported");
//            }
//        }
//    }
//    private static final DataHandlerFactory FACTORY = new DataHandlerFactory();
//    protected FileHandler fileHandler = new FileHandler();
//
//    public abstract List<DataEntity> getDataList(ProjectEntity project);
//
//    public abstract List<DataEntity> getNewlyAddedDataList(ProjectEntity project);
//
//    protected DataHandler(){}
//
//    public static DataHandler getDataHandler(int annoType)
//    {
//        try
//        {
//            return FACTORY.getDataHandler(annoType);
//        } catch (NotSupportedDataTypeException e)
//        {
//            log.error(e.getMessage());
//            return null;
//        }
//    }
//
//    public abstract String generateDataSource(DataEntity data);
//
//    public static List<String> getDataIdList(List<DataEntity> dataList)
//    {
//        return dataList.stream()
//                .map(DataEntity::getId)
//                .map(UUID::toString)
//                .collect(Collectors.toList());
//    }
}
