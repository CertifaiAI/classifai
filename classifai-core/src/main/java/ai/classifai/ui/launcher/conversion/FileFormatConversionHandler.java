package ai.classifai.ui.launcher.conversion;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileFormatConversionHandler
{
    public static void convert(@NonNull String inputFolder, @NonNull String inputFormat, @NonNull String outputFolder, @NonNull String outputFormat)
    {
        System.out.println(inputFolder);
        System.out.println(inputFormat);
        System.out.println(outputFolder);
        System.out.println(outputFormat);
    }
}
