package ai.classifai.ui;

import ai.classifai.action.ProjectImport;
import ai.classifai.ui.component.LookFeelSetter;
import ai.classifai.ui.enums.FileSystemStatus;
import ai.classifai.ui.enums.RunningStatus;
import ai.classifai.ui.enums.SelectionWindowStatus;
import ai.classifai.ui.launcher.WelcomeLauncher;
import ai.classifai.ui.launcher.conversion.ConverterLauncher;
import ai.classifai.ui.selector.project.LabelFileSelector;
import ai.classifai.ui.selector.project.ProjectFolderSelector;
import ai.classifai.ui.selector.project.ProjectImportSelector;
import ai.classifai.ui.selector.project.TabularFileSelector;
import ai.classifai.ui.utils.UIResources;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

import static javax.swing.JOptionPane.showMessageDialog;

@Slf4j
public class DesktopUI implements NativeUI {
    private final WelcomeLauncher welcomeLauncher;
    private final ProjectImportSelector projectImportSelector;
    private final ProjectFolderSelector projectFolderSelector;
    private final LabelFileSelector labelFileSelector;
    private final TabularFileSelector tabularFileSelector;

    public DesktopUI(Runnable serverShutdownCallback, ProjectImport projectImport){
        LookFeelSetter.setDarkMode();
        welcomeLauncher = new WelcomeLauncher(serverShutdownCallback, new ConverterLauncher(this));

        projectImportSelector = new ProjectImportSelector(this, projectImport);
        projectFolderSelector = new ProjectFolderSelector(this);
        labelFileSelector = new LabelFileSelector(this);
        tabularFileSelector = new TabularFileSelector(this);
    }

    @Override
    public void start() {
        welcomeLauncher.start();
    }

    @Override
    public void setRunningStatus(RunningStatus status) {
        welcomeLauncher.setRunningStatus(status);
    }

    @Override
    public void showPopupAndLog(String title, String message, int popupType) {
        log.debug(message);

        ensureWelcomeLauncherStaysInBackground();
        showMessageDialog(getFrameAtMousePointer(), message, title, popupType);
    }

    @Override
    public void showProjectImportSelector() {
        projectImportSelector.run();
    }

    @Override
    public FileSystemStatus getProjectImportStatus() {
        return projectImportSelector.getImportFileSystemStatus();
    }

    @Override
    public String getImportedProjectName() {
        return projectImportSelector.getProjectName();
    }

    @Override
    public void showLabelFileSelector() {
        labelFileSelector.run();
    }

    @Override
    public boolean isLabelFileSelectorOpen() {
        return labelFileSelector.isWindowOpen();
    }

    @Override
    public SelectionWindowStatus getLabelFileSelectorWindowStatus() {
        return labelFileSelector.getWindowStatus();
    }

    @Override
    public String getLabelFileSelectedPath() {
        return labelFileSelector.getLabelFilePath();
    }

    @Override
    public void showProjectFolderSelector() {
        projectFolderSelector.run();
    }

    @Override
    public boolean isProjectFolderSelectorOpen() {
        return projectFolderSelector.isWindowOpen();
    }

    @Override
    public SelectionWindowStatus getProjectFolderSelectorWindowStatus() {
        return projectFolderSelector.getWindowStatus();
    }

    @Override
    public String getProjectFolderSelectedPath() {
        return projectFolderSelector.getProjectFolderPath();
    }

    @Override
    public void showTabularFileSelector() {
        tabularFileSelector.run();
    }

    @Override
    public boolean isTabularFileSelectorOpen() {
        return tabularFileSelector.isWindowOpen();
    }

    @Override
    public SelectionWindowStatus getTabularFileSelectorWindowStatus() {
        return tabularFileSelector.getWindowStatus();
    }

    @Override
    public String getTabularFileSelectedPath() {
        return tabularFileSelector.getTabularFilePath();
    }

    public void ensureWelcomeLauncherStaysInBackground(){
        welcomeLauncher.setToBackground();
    }

    public JFrame getFrameAtMousePointer(){
        Point pt = MouseInfo.getPointerInfo().getLocation();
        JFrame frame = new JFrame();
        frame.setIconImage(UIResources.getClassifaiIcon());

        frame.setAlwaysOnTop(true);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setLocation(pt);
        frame.requestFocus();
        frame.setVisible(false);

        return frame;
    }
}
