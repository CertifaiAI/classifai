package ai.classifai.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Handling list operation
 *
 * @author Chiawei Lim
 */
public class ListHandler
{
    public static <T> List<T> convertListToUniqueList(List<T> list)
    {
        // create an empty set
        Set<T> set = new HashSet<>();

        for (T t : list)
            set.add(t);

        return ConversionHandler.set2List(set);
    }
}
