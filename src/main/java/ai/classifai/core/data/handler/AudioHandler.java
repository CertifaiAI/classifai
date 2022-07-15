package ai.classifai.core.data.handler;

import ai.classifai.core.dto.AudioDTO;
import ai.classifai.core.loader.ProjectLoader;
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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class AudioHandler {
    private float audioDuration = 0f;
    private float frameRate = 0;
    private int frameSize = 0;
    private int channel = 0;
    private float sampleRate = 0;
    private int sampleSizeInBits = 0;
    private List<Double> timeStampList;

    public List<Integer> generateWaveFormPeaks(String filePath) throws IOException, UnsupportedAudioFileException {
        String audioWaveFormExecutable = getAudioWaveformExecutable();
        File audioFile = new File(filePath);
        String outputFileString = audioFile.getParent() + File.separator + FilenameUtils.getBaseName(audioFile.getName()) + ".json";
        File outputFile = new File(outputFileString);

        BBCAudioWaveform bbcAudioWaveform = new BBCAudioWaveform(audioWaveFormExecutable);
        AWFCommand command = AWFCommand.builder()
                .setInput(audioFile)
                .setOutput(outputFile)
                .setBits(AWFBit.EIGHT)
                .build();

        if (bbcAudioWaveform.run(command))
        {
           log.info("audio metadata json file generated");
        }
        else
        {
            log.info("Fail to execute waveform peaks decoding");
        }

        if (outputFile.exists())
        {
            getAudioDuration(new File(filePath));
            return getWaveFormPeaks(outputFile);
        }
        else
        {
            throw new IllegalStateException("Fail to generated wave form peaks");
        }
    }

    private String getAudioWaveformExecutable() {
        return Objects.requireNonNull(AudioHandler.class.getResource("/executable/audiowaveform.exe")).getPath();
    }

    private List<Integer> getWaveFormPeaks(File outputFile) throws IOException {
        FileReader fileReader = new FileReader(outputFile);
        JsonObject jsonObject = new JsonObject(IOUtils.toString(fileReader));
        String dataListString = jsonObject.getString("data");
        String modifyDataListString = StringUtils.removeStart(StringUtils.removeEnd(dataListString, "]"), "[");
        List<String> arr = Arrays.asList(modifyDataListString.split(","));
        timeStampList = generateListOfTimeStamp(arr.size());
        return arr.stream().map(String::strip).map(Integer::parseInt).collect(Collectors.toList());
    }

    public List<Double> getTimeStamp() {
        return this.timeStampList;
    }

    private void getAudioDuration(File file) throws IOException, UnsupportedAudioFileException {
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
        AudioFormat format = audioInputStream.getFormat();

        long audioFileLength = file.length();
        this.frameSize = format.getFrameSize();
        this.frameRate = format.getFrameRate();
        this.audioDuration = audioFileLength / (frameSize * frameRate);
        this.sampleRate = format.getSampleRate();
        this.sampleSizeInBits = format.getSampleSizeInBits();
        this.channel = format.getChannels();
    }

    private List<Double> generateListOfTimeStamp(Integer dataPointLength) {
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

    public AudioDTO toDTO(ProjectLoader loader) {
        return AudioDTO.builder()
                .projectId(loader.getProjectId())
                .audioPath(loader.getProjectFilePath().toString())
                .audioDuration(audioDuration)
                .frameRate(frameRate)
                .frameSize(frameSize)
                .channel(channel)
                .sampleRate(sampleRate)
                .sampleSizeInBit(sampleSizeInBits)
                .audioRegionsPropertiesList(Collections.emptyList())
                .build();
    }
}
