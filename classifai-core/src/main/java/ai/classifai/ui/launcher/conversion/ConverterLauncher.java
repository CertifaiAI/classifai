/*
 * Copyright (c) 2020 CertifAI Sdn. Bhd.
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

import ai.classifai.selector.conversion.ConverterFolderSelector;
import ai.classifai.ui.launcher.LogoHandler;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.type.FileFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;

/**
 * Converter to convert files from Welcome Launcher
 *
 * @author codenamewei
 */
@Slf4j
@NoArgsConstructor
public class ConverterLauncher extends JPanel
{
    private static ConverterFolderSelector folderSelector;

    @Getter private static boolean isOpened;

    private static final int MAX_PAGE = 20;

    private final String FONT_TYPE = "Serif";//Serif, SansSerif, Monospaced, Dialog, and DialogInput.

    private static final int ELEMENT_HEIGHT = 40;
    private static final int TEXT_FIELD_LENGTH = 23;

    //JTextField.getText()
    private static JTextField inputFolderField = new JTextField(TEXT_FIELD_LENGTH);
    private static JTextField outputFolderField = new JTextField(TEXT_FIELD_LENGTH);

    private final static String DEFAULT_OUTPUT_PATH = "Same as source file";

    private JLabel inputFolderLabel =  new JLabel("Input Folder     : ");
    private JLabel outputFolderLabel = new JLabel("Output Folder  : ");

    private static JButton inputBrowserButton;
    private static JButton outputBrowserButton;

    private static JComboBox inputFormatCombo;
    private static JComboBox outputFormatCombo;

    private static InputFolderListener inputFolderListener;
    private static OutputFolderListener outputFolderListener;

    private JLabel maxPage = new JLabel("Maximum Page: ");
    private JTextField maxPageTextField = new JTextField();

    @Getter private static JTextArea taskOutput;
    private JScrollPane progressPane;

    private JProgressBar progressBar = new JProgressBar(0, 100);
    private static JButton convertButton = new JButton("Convert");

    private static boolean isConvertButtonClicked = false;

    private Task task;
    private JFrame frame;

    static {

        String gap = "   ";
        String[] inputFormat = new String[] {
                gap + FileFormat.PDF.getUpperCase(),
                gap + FileFormat.TIF.getUpperCase()
        };

        String[] outputFormat = new String[] {
                gap + FileFormat.JPG.getUpperCase(),
                gap + FileFormat.PNG.getUpperCase()
        };

        inputFormatCombo = new JComboBox(inputFormat);
        outputFormatCombo = new JComboBox(outputFormat);

        inputBrowserButton = new JButton("Browse");
        outputBrowserButton = new JButton("Browse");

        taskOutput = new JTextArea(105, 20);

        isOpened = false;

        Thread inputFolderThread = new Thread(() -> folderSelector = new ConverterFolderSelector());
        inputFolderThread.start();
    }

    public static void setInputFolderPath(String inputPath)
    {
        inputFolderField.setText(inputPath);
    }

    public static void setOutputFolderPath(String outputPath)
    {
        outputFolderField.setText(outputPath);
    }

    public static String getDefaultOutputPath()
    {
        return DEFAULT_OUTPUT_PATH;
    }

    public static int getMaxPage()
    {
        return MAX_PAGE;
    }

    public static String getInputFormat()
    {
        String inputFormat = (String) inputFormatCombo.getSelectedItem();

        return inputFormat.trim().toLowerCase();
    }

    public static String getOutputFormat()
    {
        String outputFormat = (String) outputFormatCombo.getSelectedItem();

        return outputFormat.trim().toLowerCase();
    }

    public void start()
    {
        frame = new JFrame("Files Format Converter");

        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowOpened(WindowEvent e)
            {
                super.windowOpened(e);

                isOpened = true;
            }

            @Override
            public void windowClosing(WindowEvent e)
            {
                isOpened = false;
                if(task != null)
                {
                    Task.stop();
                    task.cancel(true);
                }
            }
        });

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(new Dimension(640, 420));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(5, 10, 5, 10);

        constraints.gridx = 0; constraints.gridy = 0;
        panel.add(inputFormatCombo, constraints);

        constraints.gridx = 1;
        panel.add(inputFolderLabel, constraints);

        constraints.gridx = 2;
        panel.add(inputFolderField, constraints);

        constraints.gridx = 3;
        panel.add(inputBrowserButton, constraints);

        constraints.gridx = 0; constraints.gridy = 1;
        panel.add(outputFormatCombo, constraints);

        constraints.gridx = 1;
        panel.add(outputFolderLabel, constraints);

        constraints.gridx = 2;
        panel.add(outputFolderField, constraints);

        constraints.gridx = 3;
        panel.add(outputBrowserButton, constraints);

        constraints.gridwidth = 2;

        constraints.gridx = 1; constraints.gridy = 2;
        panel.add(maxPage, constraints);

        constraints.gridx = 2;
        panel.add(maxPageTextField, constraints);

        constraints.fill = GridBagConstraints.HORIZONTAL;

        constraints.gridwidth = 4;

        constraints.gridx = 0; constraints.gridy = 3;
        panel.add(progressPane, constraints);

        progressBar.setValue(0);

        constraints.gridwidth = 3;
        constraints.gridx = 0; constraints.gridy = 4;
        panel.add(progressBar, constraints);

        constraints.gridwidth = 1;
        constraints.gridx = 3; constraints.gridy = 4;
        panel.add(convertButton, constraints);

        frame.add(panel);

        frame.setIconImage(LogoHandler.getClassifaiIcon());
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    private void configure()
    {
        inputFormatCombo.setSelectedIndex(0);
        outputFormatCombo.setSelectedIndex(1);

        design(inputFolderLabel);
        design(outputFolderLabel);

        design(inputFolderField);
        inputFolderField.setEditable(false);
        inputFolderField.setText(ParamConfig.getFileSysRootSearchPath().getAbsolutePath() + File.separator + "Desktop");

        Dimension folderDimension = new Dimension(250, ELEMENT_HEIGHT - 10);
        inputFolderField.setMinimumSize(folderDimension);

        design(outputFolderField);
        outputFolderField.setText(getDefaultOutputPath());
        outputFolderField.setMinimumSize(folderDimension);

        design(inputBrowserButton);
        if(inputFolderListener == null)
        {
            inputFolderListener = new InputFolderListener();
            inputBrowserButton.addActionListener(inputFolderListener);
        }

        design(outputBrowserButton);
        if(outputFolderListener == null)
        {
            outputFolderListener = new OutputFolderListener();
            outputBrowserButton.addActionListener(outputFolderListener);
        }

        design(inputFormatCombo);
        design(outputFormatCombo);

        design(maxPage);
        design(maxPageTextField);
        maxPageTextField.setText("  " + MAX_PAGE);
        maxPageTextField.setMinimumSize(new Dimension(60, ELEMENT_HEIGHT - 10));

        design(taskOutput);

        progressPane = new JScrollPane(taskOutput);
        design(progressPane);

        //Call setStringPainted now so that the progress bar height
        //stays the same whether or not the string is shown.
        progressBar.setStringPainted(true);

        design(progressBar);
        design(convertButton);

        convertButton.addActionListener(new ConvertButtonListener());
    }

    private void design(Object obj)
    {

        if(obj instanceof JLabel)
        {
            Font font = new Font(FONT_TYPE, Font.BOLD, 14);

            Dimension dimension = new Dimension(120, ELEMENT_HEIGHT);

            JLabel label = (JLabel) obj;
            label.setFont(font);
            label.setPreferredSize(dimension);
        }
        else if(obj instanceof JTextField)
        {
            Font font = new Font(FONT_TYPE, Font.BOLD, 12);

            JTextField textField = (JTextField) obj;
            textField.setFont(font);
            textField.setBackground(Color.DARK_GRAY);
            textField.setEditable(false);
        }
        else if(obj instanceof JButton)
        {
            Font font = new Font(FONT_TYPE, Font.BOLD, 14);

            Dimension dimension = new Dimension(100, ELEMENT_HEIGHT);

            JButton button = (JButton) obj;
            button.setFont(font);
            button.setPreferredSize(dimension);
        }
        else if(obj instanceof JComboBox)
        {
            Font font = new Font(FONT_TYPE, Font.BOLD, 14);

            JComboBox comboBox = (JComboBox) obj;
            comboBox.setFont(font);

            Dimension dimension = new Dimension(80, ELEMENT_HEIGHT);

            comboBox.setMaximumSize(dimension);
        }
        else if(obj instanceof JProgressBar)
        {
            Dimension dimension = new Dimension(100, ELEMENT_HEIGHT / 2);

            Font font = new Font(FONT_TYPE, Font.PLAIN, 14);

            JProgressBar progressBar = (JProgressBar) obj;
            progressBar.setFont(font);

            progressBar.setForeground(Color.YELLOW);
            progressBar.setPreferredSize(dimension);

            progressBar.setBorderPainted(true);

            progressBar.setValue(0);
            progressBar.setUI(new BasicProgressBarUI() {
                protected Color getSelectionBackground() { return Color.LIGHT_GRAY; }
                protected Color getSelectionForeground() { return Color.BLACK; }
            });

            Border border = BorderFactory.createEtchedBorder(0);
            progressBar.setBorder(border);

        }
        else if(obj instanceof JTextArea)
        {
            JTextArea textArea = (JTextArea) obj;

            textArea.setMargin(new Insets(5,5,5,5));
            textArea.setEditable(false);

            textArea.setForeground(Color.WHITE);
        }
        else if(obj instanceof JComponent)
        {
            Dimension dimension = new Dimension(100, ELEMENT_HEIGHT * 5);

            Font font = new Font(FONT_TYPE, Font.BOLD, 8);

            JComponent scrollPane = (JComponent) obj;
            scrollPane.setFont(font);
            scrollPane.setMinimumSize(dimension);
        }


        if(obj == null)
        {
            log.info("Object for ConversionLauncher is null");
        }
    }


    public void launch()
    {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                configure();
                start();

                frame.setVisible(true);
            }
        });
    }

    public static void appendTaskOutput(@NonNull String message)
    {
        taskOutput.append(message + "\n");
        log.debug(message);
    }

    class ConvertButtonListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            if(!isConvertButtonClicked) //prevent multiple clicks
            {
                task = new Task();
                task.addPropertyChangeListener(this::propertyChange);
                task.execute();

                convertButton.setEnabled(false);
                convertButton.setForeground(Color.DARK_GRAY);
                progressBar.setIndeterminate(true);

                isConvertButtonClicked = true;
            }
        }

        /**
         * Invoked when task's progress property changes.
         */
        public void propertyChange(PropertyChangeEvent evt)
        {
            if ("progress" == evt.getPropertyName())
            {
                int progress = (Integer) evt.getNewValue();
                progressBar.setIndeterminate(false);
                progressBar.setValue(progress);

                //taskOutput.append(String.format("Completed %d%% of task.\n", progress));
            }
        }
    }
    class InputFolderListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            folderSelector.run(ConversionSelection.INPUT);
        }
    }

    class OutputFolderListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            folderSelector.run(ConversionSelection.OUTPUT);
        }

    }

    public static String[] getInputExtension()
    {

        String extensionRepresentative = ((String) inputFormatCombo.getSelectedItem()).trim();

        if(extensionRepresentative.equals(FileFormat.PDF.getUpperCase()))
        {
            return new String[]{"pdf"};
        }
        else if(extensionRepresentative.equals(FileFormat.TIF.getUpperCase()))
        {
            return new String[]{"tif", "tiff"};
        }

        return null;
    }

    public static String getInputFolderPath()
    {
        return inputFolderField.getText();
    }

    public static String getOutputFolderPath()
    {
        String buffer = outputFolderField.getText();

        if(buffer.equals(DEFAULT_OUTPUT_PATH))
        {
            return null; // for same path
        }

        return buffer;
    }

    public static void enableConvertButton()
    {
        convertButton.setForeground(Color.LIGHT_GRAY);
        convertButton.setEnabled(true);

        isConvertButtonClicked = false;
    }
}