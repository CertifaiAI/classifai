package ai.classifai.frontend.ui;

import ai.classifai.frontend.ui.enums.FileSystemStatus;
import ai.classifai.frontend.ui.enums.RunningStatus;
import ai.classifai.frontend.ui.enums.SelectionWindowStatus;

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
}
