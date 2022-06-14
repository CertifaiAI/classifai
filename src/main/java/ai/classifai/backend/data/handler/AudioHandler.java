package ai.classifai.backend.data.handler;

import ai.classifai.core.dto.properties.ImageProperties;
import com.github.marc7806.wrapper.AWFBit;
import com.github.marc7806.wrapper.AWFCommand;
import com.github.marc7806.wrapper.BBCAudioWaveform;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class AudioHandler {
    private static float audioDuration = 0;
    private static List<Integer> waveFormPeaks = new ArrayList<>();
    private static List<Double> timeStampList = new ArrayList<>();

    public static void generateWaveFormPeaks(String filePath) throws IOException, UnsupportedAudioFileException {
        String audioWaveFormExecutable = Objects.requireNonNull(
                AudioHandler.class.getClassLoader().getResource("audiowaveform.exe")
        ).getPath();
        log.info(AudioHandler.class.getClassLoader().getName());
        log.info(audioWaveFormExecutable);
        File audioFile = new File(filePath);
        String outputFileString = audioFile.getParent() + File.separator + FilenameUtils.getBaseName(audioFile.getName()) + ".json";
        File outputFile = new File(outputFileString);

        BBCAudioWaveform bbcAudioWaveform = new BBCAudioWaveform(audioWaveFormExecutable);
        AWFCommand command = AWFCommand.builder()
                .setInput(audioFile)
                .setOutput(outputFile)
                .setBits(AWFBit.EIGHT)
                .build();

        if (bbcAudioWaveform.run(command)) {
           log.info("Json file generated");
        } else {
            log.info("fail to generate json file");
        }

        if (outputFile.exists()) {
            getAudioDuration(new File(filePath));
            getWaveFormPeaks(outputFile);
        }
    }

    private static void getWaveFormPeaks(File outputFile) throws IOException {
        FileReader fileReader = new FileReader(outputFile);
        JsonObject jsonObject = new JsonObject(IOUtils.toString(fileReader));
        String dataListString = jsonObject.getString("data");
        String modifyDataListString = StringUtils.removeStart(StringUtils.removeEnd(dataListString, "]"), "[");
        List<String> arr = Arrays.asList(modifyDataListString.split(","));

        waveFormPeaks = arr.stream().map(String::strip).map(Integer::parseInt).collect(Collectors.toList());
        timeStampList = generateListOfTimeStamp(arr.size());
    }

    private static void getAudioDuration(File file) throws IOException, UnsupportedAudioFileException {
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
        AudioFormat format = audioInputStream.getFormat();

        long audioFileLength = file.length();
        int frameSize = format.getFrameSize();
        float frameRate = format.getFrameRate();
        float audioDuration = audioFileLength / (frameSize * frameRate);

        log.info(String.valueOf(format.getFrameSize()));
        log.info(String.valueOf(format.getChannels()));
        log.info(String.valueOf(format.getFrameRate()));
        log.info(String.valueOf(format.getSampleSizeInBits()));
        log.info(String.valueOf(format.getSampleRate()));
        log.info(String.valueOf(format.getEncoding()));
    }

    private static List<Double> generateListOfTimeStamp(Integer dataPointLength) {
        double timeGap = roundNumber(audioDuration / dataPointLength);
        double currentTime = roundNumber(0.00000);
        List<Double> timeStampList = new ArrayList<>();

        for(int i = 0; i < dataPointLength; i++) {
            if(i == 0) {
                timeStampList.add(0, currentTime);
            }

            else {
                currentTime += timeGap;
                timeStampList.add(i, roundNumber(currentTime));
            }
        }
        return timeStampList;
    }

    public static double roundNumber(double value) {
        return new BigDecimal(value).setScale(5, RoundingMode.UP).doubleValue();
    }

//    public static void saveWaveFormPeaksToDataBase(ProjectLoader loader, AnnotationDB annotationDB, File audioFilePath) {
//        for(int i = 0; i < timeStampList.size(); i++) {
//            annotationDB.saveWavePeaksData(loader, timeStampList.get(i), waveFormPeaks.get(i), audioFilePath.getName(), i);
//        }
//    }
}
