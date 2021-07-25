package ai.classifai.view;

import ai.classifai.core.entity.dto.generic.ProjectDTO;
import ai.classifai.core.entity.dto.generic.AnnotationDTO;
import ai.classifai.core.entity.dto.generic.DataDTO;
import ai.classifai.core.entity.dto.generic.DataVersionDTO;
import io.vertx.core.json.JsonObject;

import java.util.List;

public class ImageDataView
{
    public static JsonObject generateImageDataView(ProjectDTO projectDTO, DataDTO imageDataDto, DataVersionDTO imageDataVersionDTO,
                                                   List<AnnotationDTO> imageAnnotationDTOList, String thumbnail)
    {
//        // strategy
//        JsonObject image_data_json = imageDataDto.toJson();
//
//        //
//
//        return new JsonObject()
//                .put("message", 1)
//                .put("uuid", imageDataDto.getId().toString())
//                .put("project_name", projectDTO.getName())
//                .put("img_path", imageDataDto.getPath())
        return null;
    }


}
