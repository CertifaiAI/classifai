package ai.classifai.dto.api.reader;

import ai.classifai.dto.api.reader.body.RenameDataBody;
import com.zandero.rest.reader.ValueReader;
import com.zandero.utils.StringUtils;
import com.zandero.utils.extra.JsonUtils;
import io.vertx.core.json.jackson.DatabindCodec;

public class RenameDataReader implements ValueReader<RenameDataBody> {
    @Override
    public RenameDataBody read(String s, Class<RenameDataBody> aClass) throws Throwable {
        if(StringUtils.isNullOrEmptyTrimmed(s)) {
            return null;
        }

        return JsonUtils.fromJson(s, aClass, DatabindCodec.mapper());
    }
}
