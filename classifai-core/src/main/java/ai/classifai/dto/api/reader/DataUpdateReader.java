package ai.classifai.dto.api.reader;

import ai.classifai.dto.data.ThumbnailProperties;
import com.zandero.rest.reader.ValueReader;
import com.zandero.utils.StringUtils;
import com.zandero.utils.extra.JsonUtils;
import io.vertx.core.json.jackson.DatabindCodec;

public class DataUpdateReader implements ValueReader<ThumbnailProperties> {

    @Override
    public ThumbnailProperties read(String s, Class<ThumbnailProperties> aClass) {
        if(StringUtils.isNullOrEmptyTrimmed(s)) {
            return null;
        }

        return JsonUtils.fromJson(s, aClass, DatabindCodec.mapper());
    }
}
