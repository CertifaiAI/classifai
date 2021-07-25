package ai.classifai.service;

import ai.classifai.core.entity.dto.generic.VersionDTO;
import ai.classifai.core.entity.dto.image.ImageDataDTO;
import ai.classifai.core.entity.dto.image.ImageDataVersionDTO;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.List;

public class ImageDataVersionService extends AbstractVertxService
{
    public ImageDataVersionService(Vertx vertx)
    {
        super(vertx);
    }

    public Future<List<ImageDataVersionDTO>> getDataVersionDTOList(List<ImageDataDTO> dataDTOList, VersionDTO versionDTO)
    {
        return null;
    }
}
