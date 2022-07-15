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
public class AudioFileSelector extends SelectionWindow {
    private static final FileNameExtensionFilter AUDIO_FILE_FILTER = new FileNameExtensionFilter(
            "Tabular Files", "mp3", "wav");

    @Setter private File audioFile = null;

    public AudioFileSelector(DesktopUI ui) {
        super(ui);
    }

    public String getAudioFilePath()
    {
        return (audioFile != null) ? audioFile.getAbsolutePath() : "";
    }

    public void run()
    {
        try
        {
            EventQueue.invokeLater(() -> {

                if(windowStatus.equals(SelectionWindowStatus.WINDOW_CLOSE))
                {
                    windowStatus = SelectionWindowStatus.WINDOW_OPEN;

                    audioFile = null;

                    JFrame frame = ui.getFrameAtMousePointer();
                    String title = "Select Label File (*.mp3, *.wav)";
                    JFileChooser chooser = initChooser(JFileChooser.FILES_ONLY, title);
                    chooser.setFileFilter(AUDIO_FILE_FILTER);

                    //Important: prevent Welcome Console from popping out
                    ui.ensureWelcomeLauncherStaysInBackground();

                    int res = chooser.showOpenDialog(frame);
                    frame.dispose();

                    if (res == JFileChooser.APPROVE_OPTION)
                    {
                        audioFile = chooser.getSelectedFile();
                    }
                    else
                    {
                        log.debug("Operation of import tabular file aborted");
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
            log.info("TabularFileSelector failed to open", e);
        }
    }

}
