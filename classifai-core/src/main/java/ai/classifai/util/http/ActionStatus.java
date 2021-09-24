package ai.classifai.util.http;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

public interface ActionStatus {

    static ActionStatus ok(){
        return new ActionSucceeded();
    }

    static ActionStatus okWithResponse(Object object){
        return new ActionSucceededWithMessage(object);
    }

    static ActionStatus failedWithMessage(String msg){
        return new ActionFailed(msg);
    }
}

@Data
class ActionSucceeded implements ActionStatus {
    private final int message = 1;
}

@Data
@AllArgsConstructor
class ActionSucceededWithMessage implements ActionStatus {
    private final int message = 1;
    private Object object;
}

@Data
@AllArgsConstructor
class ActionFailed implements ActionStatus {
    private final int message = 0;
    private final int errorCode = 1;
    private final String errorMessage;
}
