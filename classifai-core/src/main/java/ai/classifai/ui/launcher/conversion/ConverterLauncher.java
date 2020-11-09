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
import java.beans.PropertyChangeListener;
import java.io.File;

/**
 * Converter to convert files from Welcome Launcher
 *
 * @author codenamewei
 */
@Slf4j
public class ConverterLauncher extends JPanel
        implements ActionListener,
        PropertyChangeListener
{
    private ConverterFolderSelector inputFolderSelector;
    private ConverterFolderSelector outputFolderSelector;

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

    private JButton inputBrowserButton = new JButton("Browse");
    private JButton outputBrowserButton = new JButton("Browse");

    private static JComboBox inputFormatCombo;
    private static JComboBox outputFormatCombo;

    private JLabel maxPage = new JLabel("Maximum Page: ");
    private JTextField maxPageTextField = new JTextField("   " + MAX_PAGE);

    @Getter private static JTextArea taskOutput;
    private JScrollPane progressPane;

    private JProgressBar progressBar = new JProgressBar(0, 100);
    @Getter private static JButton convertButton = new JButton("Convert");

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

        isOpened = false;
    }

    public ConverterLauncher()
    {
        Thread inputFolderThread = new Thread(() -> inputFolderSelector = new ConverterFolderSelector());
        inputFolderThread.start();

        Thread outputFolderThread = new Thread(() -> outputFolderSelector = new ConverterFolderSelector());
        outputFolderThread.start();
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

        frame.add(panel);//, BorderLayout.PAGE_START);

        frame.setIconImage(LogoHandler.getClassifaiIcon());
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);

    }

    private void configure()
    {
        outputFolderField.setText(DEFAULT_OUTPUT_PATH);

        inputFormatCombo.setSelectedIndex(0);
        outputFormatCombo.setSelectedIndex(0);

        convertButton.setOpaque(true);

        design(inputFolderLabel);
        design(outputFolderLabel);

        design(inputFolderField);
        inputFolderField.setEditable(false);
        inputFolderField.setText(ParamConfig.getFileSysRootSearchPath().getAbsolutePath() + File.separator + "Desktop");

        Dimension folderDimension = new Dimension(100, ELEMENT_HEIGHT - 10);
        inputFolderField.setPreferredSize(folderDimension);

        design(outputFolderField);
        outputFolderField.setPreferredSize(folderDimension);

        design(inputBrowserButton);
        inputBrowserButton.addActionListener(new InputFolderListener());

        design(outputBrowserButton);
        outputBrowserButton.addActionListener(new OutputFolderListener());

        design(inputFormatCombo);
        design(outputFormatCombo);

        design(maxPage);
        design(maxPageTextField);
        maxPageTextField.setPreferredSize(new Dimension(50, ELEMENT_HEIGHT - 10));

        taskOutput = new JTextArea(5, 20);
        taskOutput.setMargin(new Insets(5,5,5,5));
        taskOutput.setEditable(false);

        progressPane = new JScrollPane(taskOutput);
        design(progressPane);

        progressBar.setValue(0);
        progressBar.setUI(new BasicProgressBarUI() {
            protected Color getSelectionBackground() { return Color.DARK_GRAY; }
            protected Color getSelectionForeground() { return Color.BLACK; }
        });

        //Call setStringPainted now so that the progress bar height
        //stays the same whether or not the string is shown.
        progressBar.setStringPainted(true);

        design(progressBar);
        design(convertButton);

        convertButton.addActionListener(this);
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
            textField.setBackground(Color.WHITE);
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

            progressBar.setForeground(Color.GREEN);
            progressBar.setPreferredSize(dimension);


            progressBar.setBorderPainted(true);

                Border border = BorderFactory.createEtchedBorder(0);
            progressBar.setBorder(border);
        }
        else if(obj instanceof JComponent)
        {
            Dimension dimension = new Dimension(100, ELEMENT_HEIGHT * 5);

            Font font = new Font(FONT_TYPE, Font.BOLD, 8);

            JComponent scrollPane = (JComponent) obj;
            scrollPane.setFont(font);
            scrollPane.setPreferredSize(dimension);

        }

        if(obj == null)
        {
            log.info("Object for ConversionLauncher is null");
        }
    }


    public void launch()
    {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e)
        {
            log.info("Exception when setting look for ConverterLauncher: ", e);
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                configure();
                start();


                frame.setVisible(true);
            }
        });
    }

    /**
     * Invoked when the user presses the start button.
     */
    public void actionPerformed(ActionEvent evt)
    {

        convertButton.setEnabled(false);
        convertButton.setForeground(Color.LIGHT_GRAY);
        progressBar.setIndeterminate(true);


        task = new Task();
        task.addPropertyChangeListener(this::propertyChange);
        task.execute();
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

    public static void appendTaskOutput(@NonNull String message)
    {
        taskOutput.append(message + "\n");
        log.debug(message);
    }

    class InputFolderListener implements ActionListener {
        public void actionPerformed(ActionEvent e)
        {
            inputFolderSelector.run(ConversionSelection.INPUT);
        }
    }

    class OutputFolderListener implements ActionListener {
        public void actionPerformed(ActionEvent e)
        {
            outputFolderSelector.run(ConversionSelection.OUTPUT);
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
            buffer = inputFolderField.getText();
        }

        return buffer;
    }
}