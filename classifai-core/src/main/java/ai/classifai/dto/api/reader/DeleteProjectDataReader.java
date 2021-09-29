package ai.classifai.dto.api.reader;

import ai.classifai.dto.api.reader.body.DeleteProjectDataBody;
import com.zandero.rest.reader.ValueReader;
import com.zandero.utils.StringUtils;
import com.zandero.utils.extra.JsonUtils;
import io.vertx.core.json.jackson.DatabindCodec;

public class DeleteProjectDataReader implements ValueReader<DeleteProjectDataBody> {
    @Override
    public DeleteProjectDataBody read(String s, Class<DeleteProjectDataBody> aClass) throws Throwable {
        if(StringUtils.isNullOrEmptyTrimmed(s)) {
            return null;
        }

        return JsonUtils.fromJson(s, aClass, DatabindCodec.mapper());
    }
}
