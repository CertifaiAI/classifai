package ai.classifai.router;

import ai.classifai.util.http.HTTPResponseHandler;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.type.AnnotationType;
import io.vertx.ext.web.RoutingContext;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Utils {

    public boolean checkIfProjectNull(RoutingContext context, Object project, @NonNull String projectName)
    {
        if(project == null)
        {
            HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Project not found: " + projectName));

            return true;
        }

        return false;
    }

    public AnnotationType getAnnotationType(String annotation)
    {
        AnnotationType type = null;
        log.info("DEVEN: Annotation: " + annotation);
        if(annotation.equals("bndbox"))
        {
            log.info("DEVEN: BOUNDINGBOX");
            type = AnnotationType.BOUNDINGBOX;
        }
        else if(annotation.equals("seg"))
        {
            log.info("DEVEN: SEGMENTATION");
            type = AnnotationType.SEGMENTATION;
        }
        return type;
    }
}
