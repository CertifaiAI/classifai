package ai.classifai.db.entities.annotation;

public class AnnotationListFactory
{
//    public AnnotationListFactory() {}
//
//    public List<AnnotationEntity> getAnnotationListFromJson(JsonObject request, DataVersionEntity dataVersionEntity)
//    {
//        if (request.containsKey(BoundingBoxAnnotationEntity.META_KEY))
//        {
//            List<JsonObject> jsonList = getAnnotationJsonList(request.getJsonArray(BoundingBoxAnnotationEntity.META_KEY));
//            return IntStream.range(0, jsonList.size())
//                    .mapToObj(i ->
//                            BoundingBoxAnnotationEntity.getAnnotationFromJson(jsonList.get(i), dataVersionEntity, i))
//                    .collect(Collectors.toList());
//        }
//        else
//        {
//            throw new IllegalArgumentException("Annotation key not found!");
//        }
//    }
//
//    private List<JsonObject> getAnnotationJsonList(JsonArray arr)
//    {
//        return arr.stream()
//                .map(obj -> (JsonObject) obj)
//                .collect(Collectors.toList());
//    }
}
