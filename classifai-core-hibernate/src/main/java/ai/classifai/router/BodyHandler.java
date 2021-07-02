package ai.classifai.router;

import ai.classifai.util.type.AnnotationHandler;
import ai.classifai.util.type.AnnotationType;

public class BodyHandler extends EndpointVariableHandler
{
    public AnnotationType getAnnoType(String annoString)
    {
        return AnnotationHandler.getType(annoString);
    }
}
