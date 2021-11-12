package ai.classifai.core.util;

import java.net.URL;
import java.net.URLConnection;

/**
 * Check if internet is connected
 *
 * @authpr codenamewei
 */
public class InternetConnection
{
    private final static String URL = "http://www.google.com";

    public static boolean isOn()
    {
        try
        {
            URL url = new URL(URL);

            URLConnection connection = url.openConnection();

            connection.connect();

            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }
}
