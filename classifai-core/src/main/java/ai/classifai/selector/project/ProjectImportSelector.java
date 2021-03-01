package ai.classifai.selector.project;

import ai.classifai.action.ProjectImport;
import ai.classifai.ui.launcher.LogoLauncher;
import ai.classifai.ui.launcher.WelcomeLauncher;
import ai.classifai.util.ParamConfig;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

@Slf4j
public class ProjectImportSelector
{
    private static FileNameExtensionFilter imgfilter = new FileNameExtensionFilter("Json Files", new String[]{"json"});

    public void run()
    {
        try
        {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {

                    Point pt = MouseInfo.getPointerInfo().getLocation();
                    JFrame frame = new JFrame();
                    frame.setIconImage(LogoLauncher.getClassifaiIcon());

                    frame.setAlwaysOnTop(true);
                    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    frame.setLocation(pt);
                    frame.requestFocus();
                    frame.setVisible(false);

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
                    chooser.setFileFilter(imgfilter);
                    chooser.setDialogTitle("Select Files");
                    chooser.setMultiSelectionEnabled(false);
                    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    chooser.setAcceptAllFileFilterUsed(false);

                    //Important: prevent Welcome Console from popping out
                    WelcomeLauncher.setToBackground();

                    int res = chooser.showOpenDialog(frame);
                    frame.dispose();

                    if (res == JFileChooser.APPROVE_OPTION)
                    {
                        File jsonFile =  chooser.getSelectedFile().getAbsoluteFile();

                        if ((jsonFile != null) && (jsonFile.exists()))
                        {
                            log.info("Proceed with importing project with " + jsonFile.getName());

                            ProjectImport.importProjectFile(jsonFile);
                        }
                        else
                        {
                            log.debug("Import project failed");
                        }
                    }
                    else
                    {
                        //TODO
                    }
                }
            });
        }
        catch (Exception e)
        {
            log.info("ProjectHandler for File type failed to open", e);
        }
    }

}
