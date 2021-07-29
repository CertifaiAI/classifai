/*
 * Copyright (c) 2020-2021 CertifAI Sdn. Bhd.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.classifai.ui.launcher.conversion;

import ai.classifai.util.data.FileHandler;
import ai.classifai.util.data.PdfHandler;
import ai.classifai.util.data.TifHandler;
import ai.classifai.util.type.FileFormat;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.io.File;
import java.util.List;

/**
 * Task running in background to do the conversion
 *
 * @author codenamewei
 */
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
        setProgress(0);

        //iterate to get number of files

        String[] inputExtension = ConverterLauncher.getInputExtension();

        File inputPath = new File(ConverterLauncher.getInputFolderPath());

        FileHandler fileHandler = new FileHandler();

        List<String> inputFiles = fileHandler.processFolder(inputPath, inputExtension);

        ConverterLauncher.appendTaskOutput("Total number of files to convert: " + inputFiles.size());

        if (inputFiles.isEmpty())
        {
            ConverterLauncher.appendTaskOutput("Input file lists empty. Task completed!");
            setProgress(100);
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

            if (inputFormat.equals(FileFormat.PDF.getText()))
            {
                PdfHandler pdfHandler = new PdfHandler();

                for (String input: inputFiles)
                {
                    File file = new File(input);

                    if(isStop) break;

                    ConverterLauncher.appendTaskOutput(file.getName());

                    String message = pdfHandler.savePdf2Image(file, outputFolderPath, outputFormat);

                    if(message != null) ConverterLauncher.appendTaskOutput(message);

                    progress = (int) ((++fileProcessed / (double) inputFiles.size()) * 100);

                    setProgress(Math.min(progress, 100));
                }
            }
            else if (inputFormat.equals(FileFormat.TIF.getText()))
            {
                TifHandler tifHandler = new TifHandler();

                for (String input: inputFiles)
                {
                    File file = new File(input);

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
    @Override
    public void done()
    {
        ConverterLauncher.enableConvertButton();
        ConverterLauncher.getTaskOutput().append("Completed!\n");
    }
}