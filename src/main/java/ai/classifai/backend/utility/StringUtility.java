package ai.classifai.backend.utility;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
public class StringUtility {
    public static List<String> convertStringListToListString(String stringList) {
        List<String> labelList;
        String removeBracketStr = StringUtils.removeStart(StringUtils.removeEnd(stringList, "]"), "[");
        String[] stringArr = removeBracketStr.split(",");
        if (stringArr.length > 0) {
            labelList = new ArrayList<>(Arrays.asList(stringArr));
        } else {
            labelList = Collections.emptyList();
        }
        return labelList;
    }
}
