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
package ai.classifai.frontend.ui.launcher.conversion;

import ai.classifai.backend.utility.handler.FileHandler;
import ai.classifai.backend.utility.handler.PdfHandler;
import ai.classifai.backend.utility.handler.TifHandler;
import ai.classifai.frontend.ui.utils.FileFormat;
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
    private final ConverterLauncher converterLauncher;

    public static void stop()
    {
        isStop = true;
    }

    public Task(ConverterLauncher converterLauncher)
    {
        isStop = false;
        this.converterLauncher = converterLauncher;
    }

    @Override
    public Void doInBackground()
    {
        setProgress(0);

        //iterate to get number of files

        String[] inputExtension = converterLauncher.getInputExtension();

        File inputPath = new File(converterLauncher.getInputFolderPath());

        List<String> inputFiles = FileHandler.processFolder(inputPath, inputExtension);

        converterLauncher.appendTaskOutput("Total number of files to convert: " + inputFiles.size());

        if (inputFiles.isEmpty())
        {
            converterLauncher.appendTaskOutput("Input file lists empty. Task completed!");
            setProgress(100);
        }
        else
        {
            String inputFormat = converterLauncher.getInputFormat();

            String outputFolderPath = converterLauncher.getOutputFolderPath();

            String outputFormat = converterLauncher.getOutputFormat();

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

                    converterLauncher.appendTaskOutput(file.getName());

                    String message = pdfHandler.savePdf2Image(file, outputFolderPath, outputFormat);

                    if(message != null) converterLauncher.appendTaskOutput(message);

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

                    converterLauncher.appendTaskOutput(file.getName());

                    String message = tifHandler.saveTif2Image(file, outputFolderPath, outputFormat);

                    if(message != null) converterLauncher.appendTaskOutput(message);

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
        converterLauncher.enableConvertButton();
        //ConverterLauncher.getTaskOutput().append("Completed!\n");
    }
}