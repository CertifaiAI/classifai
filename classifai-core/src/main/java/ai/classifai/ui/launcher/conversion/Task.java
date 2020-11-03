package ai.classifai.ui.launcher.conversion;


import ai.classifai.util.data.FileHandler;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Random;

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

        String outputFolderPath = ConverterLauncher.getOutputFolderPath();

        Random random = new Random();

        int progress = 0;
        //Initialize progress property.
        setProgress(0);
        //Sleep for at least one second to simulate "startup".

        try {
            Thread.sleep(1000 + random.nextInt(2000));
        } catch (InterruptedException ignore) {}

        while (progress < 100) {
            //Sleep for up to one second.
            try {
                Thread.sleep(random.nextInt(1000));
            } catch (InterruptedException ignore) {}
            //Make random progress.
            progress += random.nextInt(10);
            setProgress(Math.min(progress, 100));
        }

        return null;
    }
    /*
     * Executed in event dispatch thread
     */
    public void done()
    {
        convertButton.setForeground(Color.BLACK);
        convertButton.setEnabled(true);

        taskOutput.append("Done!\n");
    }
}