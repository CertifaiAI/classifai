package ai.classifai.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTime
{
    public static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String now()
    {

        return LocalDateTime.now().format(FORMAT);
    }
}
