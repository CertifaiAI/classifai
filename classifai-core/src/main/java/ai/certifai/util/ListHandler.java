package ai.certifai.util;

import ch.qos.logback.core.pattern.ConverterUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Handling list operation
 */
public class ListHandler
{
    // Generic function to convert list to set
    public static <T> List<T> convertListToUniqueList(List<T> list)
    {
        // create an empty set
        Set<T> set = new HashSet<>();

        // Add each element of list into the set
        for (T t : list)
            set.add(t);

        // return the set
        return ConversionHandler.set2List(set);
    }
}
