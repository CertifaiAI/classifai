package ai.classifai.frontend.ui.selector;

import ai.classifai.core.status.SelectionWindowStatus;
import ai.classifai.frontend.ui.DesktopUI;
import ai.classifai.frontend.ui.component.SelectionWindow;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

@Slf4j
public class VideoFileSelector extends SelectionWindow {
    private static final FileNameExtensionFilter VIDEO_FILE_FILTER = new FileNameExtensionFilter(
            "Video Files", "mp4");

    @Setter
    private File videoFile = null;

    public VideoFileSelector(DesktopUI ui) {
        super(ui);
    }

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

                    JFrame frame = ui.getFrameAtMousePointer();
                    String title = "Select Label File (*.mp4)";
                    JFileChooser chooser = initChooser(JFileChooser.FILES_ONLY, title);
                    chooser.setFileFilter(VIDEO_FILE_FILTER);

                    //Important: prevent Welcome Console from popping out
                    ui.ensureWelcomeLauncherStaysInBackground();

                    int res = chooser.showOpenDialog(frame);
                    frame.dispose();

                    if (res == JFileChooser.APPROVE_OPTION)
                    {
                        videoFile = chooser.getSelectedFile();
                    }
                    else
                    {
                        log.debug("Operation of import video file aborted");
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
            log.info("VideoFileSelector failed to open", e);
        }
    }
}
