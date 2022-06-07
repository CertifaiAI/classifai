package ai.classifai.backend.utility;

import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
@NoArgsConstructor
public class StringUtility {
    public static List<String> convertStringListToListString(String stringList) {
        String removeBracketStr = StringUtils.removeStart(StringUtils.removeEnd(stringList, "]"), "[");
        String[] stringArr = removeBracketStr.split(",");
        return Arrays.asList(stringArr);
    }
}
