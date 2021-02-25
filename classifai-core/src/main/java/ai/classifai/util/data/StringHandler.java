package ai.classifai.util.data;

import lombok.NonNull;

import java.util.List;

public class StringHandler
{
    public static String cleanUpRegex(@NonNull String input, List<String> regexList)
    {
        String output = input;

        for(String regex : regexList)
        {
            output = output.replace(regex, "");
        }

        return output;
    }
}
