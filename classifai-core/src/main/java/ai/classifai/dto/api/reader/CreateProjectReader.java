package ai.classifai.dto.api.reader;

import ai.classifai.dto.api.reader.body.CreateProjectBody;
import com.zandero.rest.reader.ValueReader;
import com.zandero.utils.StringUtils;
import com.zandero.utils.extra.JsonUtils;
import io.vertx.core.json.jackson.DatabindCodec;

public class CreateProjectReader implements ValueReader<CreateProjectBody> {

    @Override
    public CreateProjectBody read(String s, Class<CreateProjectBody> aClass) throws Throwable {
        if(StringUtils.isNullOrEmptyTrimmed(s)) {
            return null;
        }

        return JsonUtils.fromJson(s, aClass, DatabindCodec.mapper());
    }
}
