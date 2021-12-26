package ai.classifai.selector.project;

import ai.classifai.selector.status.SelectionWindowStatus;
import ai.classifai.ui.SelectionWindow;
import ai.classifai.ui.launcher.WelcomeLauncher;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

@Slf4j
public class VideoFileSelector extends SelectionWindow {
    @Setter
    private File videoFile = null;

    private static final FileNameExtensionFilter imgFilter = new FileNameExtensionFilter(
            "Video File", "mp4");

    public String getVideoFilePath()
    {
        return (videoFile != null) ? videoFile.getAbsolutePath() : "";
    }

    public void run()
    {
        try
        {
            EventQueue.invokeLater(() -> {

                if(windowStatus.equals(SelectionWindowStatus.WINDOW_CLOSE))
                {
                    windowStatus = SelectionWindowStatus.WINDOW_OPEN;

                    videoFile = null;

                    JFrame frame = initFrame();
                    String title = "Select Video File (*.mp4)";
                    JFileChooser chooser = initChooser(JFileChooser.FILES_ONLY, title);
                    chooser.setFileFilter(imgFilter);

                    //Important: prevent Welcome Console from popping out
                    WelcomeLauncher.setToBackground();

                    int res = chooser.showOpenDialog(frame);
                    frame.dispose();

                    if (res == JFileChooser.APPROVE_OPTION)
                    {
                        videoFile = chooser.getSelectedFile();
                    }
                    else
                    {
                        log.debug("Operation of import project aborted");
                    }

                    windowStatus = SelectionWindowStatus.WINDOW_CLOSE;
                }
                else
                {
                    showAbortImportPopup();
                }
            });
        }
        catch (Exception e)
        {
            log.info("LabelFileSelector failed to open", e);
        }
    }

}
