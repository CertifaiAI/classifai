package ai.classifai.ui;

import ai.classifai.ui.enums.FileSystemStatus;
import ai.classifai.ui.enums.RunningStatus;
import ai.classifai.ui.enums.SelectionWindowStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ContainerUI implements NativeUI {
    @Override
    public void start() {
        log.info("Server Started...");
    }

    @Override
    public void setRunningStatus(RunningStatus status) {
        log.info("Running Status: "+status.getText());
    }

    @Override
    public void showPopupAndLog(String title, String message, int popupType) {
        switch (popupType){
            case 0:
                log.error(title);
                log.error(message);
                break;
            case 1:
                log.info(title);
                log.info(message);
                break;
            case 2:
                log.warn(title);
                log.warn(message);
                break;
            default:
                log.debug(title);
                log.debug(message);
                break;
        }
    }


    /** The following methods all do nothing when the application is running in a container
     *
     * Depending on the requirements, it may make sense to let them return a fixed value that is configured
     * on the command line when the application is started.
     * */
    @Override
    public void showProjectImportSelector() {

    }

    @Override
    public FileSystemStatus getProjectImportStatus() {
        return null;
    }

    @Override
    public String getImportedProjectName() {
        return null;
    }

    @Override
    public void showLabelFileSelector() {

    }

    @Override
    public boolean isLabelFileSelectorOpen() {
        return false;
    }

    @Override
    public SelectionWindowStatus getLabelFileSelectorWindowStatus() {
        return null;
    }

    @Override
    public String getLabelFileSelectedPath() {
        return null;
    }

    @Override
    public void showProjectFolderSelector() {

    }

    @Override
    public boolean isProjectFolderSelectorOpen() {
        return false;
    }

    @Override
    public SelectionWindowStatus getProjectFolderSelectorWindowStatus() {
        return null;
    }

    @Override
    public String getProjectFolderSelectedPath() {
        return null;
    }

    @Override
    public void showAudioFileSelector() {

    }

    @Override
    public boolean isAudioFileSelectorOpen() {
        return false;
    }

    @Override
    public SelectionWindowStatus getAudioFileSelectorWindowStatus() {
        return null;
    }

    @Override
    public String getAudioFileSelectedPath() {
        return null;
    }
}
