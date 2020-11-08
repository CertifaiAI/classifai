package ai.classifai.ui.launcher.conversion;

import ai.classifai.util.data.FileHandler;
import ai.classifai.util.data.PdfHandler;
import ai.classifai.util.type.FileFormat;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.io.File;


@Slf4j
class Task extends SwingWorker<Void, Void> {
    /*
     * Main task. Executed in background thread.
     */
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
            ConverterLauncher.appendTaskOutput("Done!");
            return null;
        }

        String inputFormat = ConverterLauncher.getInputFormat();

        String outputFolderPath = ConverterLauncher.getOutputFolderPath();

        String outputFormat = ConverterLauncher.getOutputFormat();

        int progress = 0;
        //Initialize progress property.
        setProgress(0);

        if(inputFormat.equals(FileFormat.PDF.getText()))
        {
            int fileProcessed = 0;
            for(File file: inputFiles)
            {
                String outputFileName = PdfHandler.savePdf2Image(file, outputFolderPath, outputFormat);

                progress = (int) (++fileProcessed / file.length() * 100);

                setProgress(progress);

                ConverterLauncher.appendTaskOutput(outputFileName + " completed.");
            }
        }
        else if(inputFormat.equals(FileFormat.TIF.getText()))
        {
            log.info("TIF Conversion");
        }
        else
        {
            log.info("Input file format not supported: " + inputFormat);

        }

        done();

        /*
        if(false)
        {
            Random random = new Random();


            int progress = 0;
            //Initialize progress property.
            setProgress(0);
            //Sleep for at least one second to simulate "startup".

            try {
                Thread.sleep(1000 + random.nextInt(2000));
            } catch (InterruptedException ignore) {}

            while (progress < 100)
            {
                //Sleep for up to one second.
                try {
                    Thread.sleep(random.nextInt(1000));
                } catch (InterruptedException ignore) {}
                //Make random progress.
                progress += random.nextInt(10);

                setProgress(Math.min(progress, 100));
            }

        }

        */

        return null;
    }
    /*
     * Executed in event dispatch thread
     */
    public void done()
    {
        JButton convertButton = ConverterLauncher.getConvertButton();

        convertButton.setForeground(Color.BLACK);
        convertButton.setEnabled(true);

        ConverterLauncher.getTaskOutput().append("Done!\n");
    }
}