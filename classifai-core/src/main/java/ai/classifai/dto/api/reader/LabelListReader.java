package ai.classifai.dto.api.reader;

import ai.classifai.dto.api.reader.body.LabelListBody;
import com.zandero.rest.reader.ValueReader;
import com.zandero.utils.StringUtils;
import com.zandero.utils.extra.JsonUtils;
import io.vertx.core.json.jackson.DatabindCodec;

public class LabelListReader implements ValueReader<LabelListBody> {

    @Override
    public LabelListBody read(String s, Class<LabelListBody> aClass) throws Throwable {
        if(StringUtils.isNullOrEmptyTrimmed(s)) {
            return null;
        }

        return JsonUtils.fromJson(s, aClass, DatabindCodec.mapper());
    }
}
