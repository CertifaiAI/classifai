package ai.classifai.frontend.ui;


import ai.classifai.backend.status.FileSystemStatus;
import ai.classifai.backend.status.SelectionWindowStatus;
import ai.classifai.frontend.ui.enums.RunningStatus;

public interface NativeUI {
    void start();
    void setRunningStatus(RunningStatus status);
    void showPopupAndLog(String title, String message, int popupType);

    void showProjectImportSelector();
    FileSystemStatus getProjectImportStatus();
    String getImportedProjectName();

    void showLabelFileSelector();
    boolean isLabelFileSelectorOpen();
    SelectionWindowStatus getLabelFileSelectorWindowStatus();
    String getLabelFileSelectedPath();

    void showProjectFolderSelector();
    boolean isProjectFolderSelectorOpen();
    SelectionWindowStatus getProjectFolderSelectorWindowStatus();
    String getProjectFolderSelectedPath();

    void showTabularFileSelector();
    boolean isTabularFileSelectorOpen();
    SelectionWindowStatus getTabularFileSelectorWindowStatus();
    String getTabularFileSelectedPath();

}
