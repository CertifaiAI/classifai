package ai.classifai.ui.launcher.conversion;

import ai.classifai.util.data.FileHandler;
import ai.classifai.util.data.PdfHandler;
import ai.classifai.util.data.TifHandler;
import ai.classifai.util.type.FileFormat;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.io.File;


@Slf4j
public class Task extends SwingWorker<Void, Void> {
    /*
     * Main task. Executed in background thread.
     */
    @Getter
    private static boolean isStop = false;

    public static void stop()
    {
        isStop = true;
    }

    public Task()
    {
        isStop = false;
    }

    @Override
    public Void doInBackground()
    {
        //iterate to get number of files

        String[] inputExtension = ConverterLauncher.getInputExtension();

        File inputPath = new File(ConverterLauncher.getInputFolderPath());

        java.util.List<File> inputFiles = FileHandler.processFolder(inputPath, inputExtension);

        ConverterLauncher.appendTaskOutput("Total number of files to convert: " + inputFiles.size());

        if(inputFiles.isEmpty())
        {
            ConverterLauncher.appendTaskOutput("Input file lists empty. Task completed!");
        }
        else
        {
            String inputFormat = ConverterLauncher.getInputFormat();

            String outputFolderPath = ConverterLauncher.getOutputFolderPath();

            String outputFormat = ConverterLauncher.getOutputFormat();

            int progress = 0;
            //Initialize progress property.
            setProgress(progress);

            int fileProcessed = 0;

            if(inputFormat.equals(FileFormat.PDF.getText()))
            {
                PdfHandler pdfHandler = new PdfHandler();

                for(File file: inputFiles)
                {
                    if(isStop) break;

                    ConverterLauncher.appendTaskOutput(file.getName());

                    String message = pdfHandler.savePdf2Image(file, outputFolderPath, outputFormat);

                    if(message != null) ConverterLauncher.appendTaskOutput(message);

                    progress = (int) ((++fileProcessed / (double) inputFiles.size()) * 100);

                    setProgress(Math.min(progress, 100));

                }
            }
            else if(inputFormat.equals(FileFormat.TIF.getText()))
            {
                TifHandler tifHandler = new TifHandler();

                for(File file: inputFiles)
                {
                    if(isStop) break;

                    ConverterLauncher.appendTaskOutput(file.getName());

                    String message = tifHandler.saveTif2Image(file, outputFolderPath, outputFormat);

                    if(message != null) ConverterLauncher.appendTaskOutput(message);

                    progress = (int) ((++fileProcessed / (double) inputFiles.size()) * 100);

                    setProgress(Math.min(progress, 100));

                }
            }
            else
            {
                log.info("Input file format not supported: " + inputFormat);

            }
            done();
        }

        return null;
    }
    /*
     * Executed in event dispatch thread
     */
    public void done()
    {
        ConverterLauncher.enableConvertButton();

       //ConverterLauncher.getTaskOutput().append("Completed!\n");
    }
}