package ai.classifai.frontend.ui.component.os;

import ai.classifai.core.util.ParamConfig;
import lombok.SneakyThrows;

import java.util.List;

public class Mac extends AbstractSupportedOS {
    @Override
    public boolean openLogInEditor() {
        String logPath = ParamConfig.getLogFilePath();
        String[] command = new String[]{"/usr/bin/open", "-e", logPath};

        return run(command);
    }

    @Override
    public boolean openBrowser() {
        final String browserURL = ParamConfig.getBrowserURL();

        final List<String> browserLocations = List.of(
                "/Applications/classifai.app/Contents/app/chrome-mac/Chromium.app",
                "/Applications/Google Chrome.app"
        );

        for (String browser : browserLocations)
        {
            if (isProgramPathExist(browser))
            {
                String[] command = new String[]{"/usr/bin/open", "-a", browser, browserURL};

                return run(command);
            }
        }
        return false;
    }

    @SneakyThrows
    private boolean run(String[] command){
        return isRunning(Runtime.getRuntime().exec(command));
    }
}
