package ai.classifai.ui.component.os;

import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public abstract class AbstractSupportedOS implements OS {
    protected static boolean isRunning(Process process)
    {
        try
        {
            process.exitValue();
            return false;
        }
        catch (Exception e)
        {
            return true;
        }
    }

    protected static boolean isProgramPathExist(String appPath)
    {
        if (appPath.equals("default"))
        {
            return true;
        }
        if (!new File(appPath).exists())
        {
            log.debug("Program not found - " + appPath);

            return false;
        }
        return true;
    }
}
