package ai.classifai.ui.component.os;

public class UnsupportedOS implements OS {
    @Override
    public boolean openLogInEditor() {
        return false;
    }

    @Override
    public boolean openBrowser() {
        return false;
    }
}
