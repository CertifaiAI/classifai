package ai.classifai.ui;

import ai.classifai.ui.launcher.LogoLauncher;
import ai.classifai.util.ParamConfig;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;

public class SelectionWindow {

    public enum ImportSelectionWindowStatus
    {
        WINDOW_OPEN,
        WINDOW_CLOSE
    }
    public ImportSelectionWindowStatus windowStatus = ImportSelectionWindowStatus.WINDOW_CLOSE;

    private final FileNameExtensionFilter imgFilter = new FileNameExtensionFilter(
            "Json Files", "json");

    public JFrame initFrame()
    {
        Point pt = MouseInfo.getPointerInfo().getLocation();
        JFrame frame = new JFrame();
        frame.setIconImage(LogoLauncher.getClassifaiIcon());

        frame.setAlwaysOnTop(true);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setLocation(pt);
        frame.requestFocus();
        frame.setVisible(false);

        return frame;
    }

    public JFileChooser initChooser(int mode)
    {
        JFileChooser chooser = new JFileChooser() {
            @Override
            protected JDialog createDialog(Component parent)
                    throws HeadlessException {
                JDialog dialog = super.createDialog(parent);
                dialog.setLocationByPlatform(true);
                dialog.setAlwaysOnTop(true);
                return dialog;
            }
        };

        chooser.setCurrentDirectory(ParamConfig.getRootSearchPath());
        chooser.setFileFilter(imgFilter);
        chooser.setDialogTitle("Select Files");
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(mode);
        chooser.setAcceptAllFileFilterUsed(false);

        return chooser;
    }
}
