package ai.classifai.frontend.ui.component.os;

import ai.classifai.core.utility.ParamConfig;
import lombok.SneakyThrows;

import java.util.List;

public class Windows extends AbstractSupportedOS {
    @Override
    public boolean openLogInEditor() {
        String logPath = ParamConfig.getLogFilePath();
        final List<String> editorLocations = List.of("C:\\Windows\\System32\\notepad.exe", "C:\\Windows\\notepad.exe");
        for (String editor : editorLocations)
        {
            if (isProgramPathExist(editor))
            {
                String command = editor + " " + logPath;
                return run(command);
            }
        }
        return false;
    }

    @Override
    public boolean openBrowser() {
        final String browserURL = ParamConfig.getBrowserURL();

        final List<String> browserLocations = List.of(System.getProperty("user.home") + "\\AppData\\Local\\Google\\Chrome\\Application\\chrome.exe",
                "C:\\Program Files\\classifai\\app\\chrome-win\\chrome.exe",
                "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe",
                "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe");

        for (String browser : browserLocations)
        {
            if (isProgramPathExist(browser))
            {
                String command = browser + " " + browserURL;
                return run(command);
            }
        }
        return false;
    }

    @SneakyThrows
    private boolean run(String command){
        return isRunning(Runtime.getRuntime().exec(command));
    }
}
