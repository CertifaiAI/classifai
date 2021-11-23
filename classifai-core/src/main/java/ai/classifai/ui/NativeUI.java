package ai.classifai.ui;

import ai.classifai.ui.enums.FileSystemStatus;
import ai.classifai.ui.enums.RunningStatus;
import ai.classifai.ui.enums.SelectionWindowStatus;

import java.util.List;

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

    void showImageFileSelector();
    void clearImagePathList();
    void clearImageDirectoryList();
    boolean isImageFileSelectorOpen();
    SelectionWindowStatus getImageFileSelectorWindowStatus();
    List<String> getImagePathList();
    List<String> getImageDirectoryList();
}
