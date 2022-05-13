package ai.classifai.ui;

import ai.classifai.database.status.FileSystemStatus;
import ai.classifai.database.status.SelectionWindowStatus;
import ai.classifai.util.RunningStatus;

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
