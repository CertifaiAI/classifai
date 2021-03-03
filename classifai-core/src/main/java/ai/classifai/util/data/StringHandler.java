package ai.classifai.util.data;

import lombok.NonNull;

public class StringHandler
{
    public static String cleanUpRegex(@NonNull String input)
    {
        return input.replace("\"", "");
    }

    /*
    public static String cleanUpRegex(@NonNull String input, @Nullable List<String> regexList)
    {
        if (regexList == null) regexList = Arrays.asList("\"");

        String output = input;

        for(String regex : regexList)
        {
            output = output.replace(regex, "");
        }

        return output;
    }
     */
}
