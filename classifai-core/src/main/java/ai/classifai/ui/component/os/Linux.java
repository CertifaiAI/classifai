package ai.classifai.ui.component.os;

import ai.classifai.util.ParamConfig;
import lombok.SneakyThrows;

public class Linux extends AbstractSupportedOS {
    @Override
    public boolean openLogInEditor() {
        String logPath = ParamConfig.getLogFilePath();
        String[] command = new String[]{"gio", "open", logPath};

        return run(command);
    }

    @Override
    public boolean openBrowser() {
        final String browserURL = ParamConfig.getBrowserURL();
        final String[] command = {"gio", "open", browserURL};

        return run(command);
    }

    @SneakyThrows
    private boolean run(String[] command){
        return isRunning(Runtime.getRuntime().exec(command));
    }
}
