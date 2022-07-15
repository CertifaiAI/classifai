package ai.classifai.core.service;


import ai.classifai.core.status.FileSystemStatus;
import ai.classifai.core.status.SelectionWindowStatus;
import ai.classifai.core.enumeration.RunningStatus;

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

    void showAudioFileSelector();
    boolean isAudioFileSelectorOpen();
    SelectionWindowStatus getAudioFileSelectorWindowStatus();
    String getAudioFileSelectedPath();

    void showVideoFileSelector();
    boolean isVideoFileSelectorOpen();
    SelectionWindowStatus getVideoFileSelectorWindowStatus();
    String getVideoFileSelectedPath();

}
