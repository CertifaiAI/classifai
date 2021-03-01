package ai.classifai.util.datetime;

import io.vertx.sqlclient.Tuple;

public class DateTimeOps
{
    /*
    A compare(B)
     > 0 : A later than B
     = 0 : both time are exactly the same
     < 0 : A prior to B
     */
    public static int compare(DateTime former, DateTime latter)
    {
        int compareHour = former.getHour().compareTo(latter.getHour());
        int compareMinute = former.getMinute().compareTo(latter.getMinute());
        int compareSecond = former.getSecond().compareTo(latter.getSecond());

        if(compareHour > 0)
        {
            return 1;
        }

        if((compareHour == 0) && (compareMinute > 0))
        {
            return 1;
        }

        if((compareHour == 0) && (compareMinute == 0))
        {
            if(compareSecond > 0)
            {
                return 1;
            }
            else if(compareSecond == 0)
            {
                return 0;
            }
        }

        return -1;
    }


    public static Tuple minus(Integer former, Integer latter)
    {
        boolean isMinusRequired = false;

        int output;

        if (former.compareTo(latter) < 0)
        {
            output = 60 - latter;
            isMinusRequired = true;
        }
        else
        {
            output = former - latter;
        }

        return Tuple.of(output, isMinusRequired);
    }

    public static double getTimeInHour(DateTime time)
    {
        return time.getHour() + (time.getMinute() / 60.0) + (time.getSecond() / 360.0);
    }

    public static String intFormatToStr(Integer value)
    {
        return value < 10 ? "0" + value : String.valueOf(value);
    }
}