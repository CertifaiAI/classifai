package ai.classifai.dto.api.reader;

import ai.classifai.dto.api.reader.body.ProjectStatusBody;
import com.zandero.rest.reader.ValueReader;
import com.zandero.utils.StringUtils;
import com.zandero.utils.extra.JsonUtils;
import io.vertx.core.json.jackson.DatabindCodec;

public class ProjectStatusReader implements ValueReader<ProjectStatusBody> {

    @Override
    public ProjectStatusBody read(String s, Class<ProjectStatusBody> aClass) throws Throwable {
        if(StringUtils.isNullOrEmptyTrimmed(s)) {
            return null;
        }

        return JsonUtils.fromJson(s, aClass, DatabindCodec.mapper());
    }
}
