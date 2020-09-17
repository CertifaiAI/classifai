package ai.classifai.os;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Map;

import static javax.swing.JOptionPane.showMessageDialog;

@Slf4j
public class ProgramOpener
{
    private static ImageIcon browserNotFoundIcon;

    static
    {
        try {

            Image iconImage = ImageIO.read(BrowserHandler.class.getResource( "/icon/Classifai_Favicon_Dark_32px.png"));

            browserNotFoundIcon = new ImageIcon(iconImage);
        }
        catch (Exception e)
        {
            log.info("Classifai icon for Program Path Not Found is missing", e);
        }
    }

    public static void launch(OS currentOS, Map<String, List<String>> programKey, String param)
    {
        boolean programNotFound = true;//default as true

        if(programKey.containsKey(currentOS.name()))
        {
            List<String> browserList = programKey.get(currentOS.name());

            if((browserList == null) || (browserList.isEmpty()))
            {
                failToOpenProgramPathMessage("Program for " + currentOS.name() + " cannot be found");
                return;
            }

            for(String browser : browserList)
            {
                if((programNotFound == true) && (isProgramPathExist(browser)))
                {
                    if(tryCurrentProgramPath(currentOS, browser, param))
                    {
                        programNotFound = false;
                        break;
                    }
                }
            }

            if(programNotFound)
            {
                failToOpenProgramPathMessage("Initialization of program path failed in current OS: " + currentOS.name());
                return;
            }

        }
        else
        {
            String osNotSupportedMessage = "Current selected OS is not supported yet";
            failToOpenProgramPathMessage(osNotSupportedMessage);
            return;
        }
    }

    public static boolean tryCurrentProgramPath(OS os, String programPath, String param)
    {
        boolean isProgramAbleToRun = false;
        String[] commandPath = null;

        if(os.equals(OS.MAC))
        {
            commandPath = new String[]{"/usr/bin/open", "-a", programPath, param};
        }
        else if(os.equals(OS.WINDOWS))
        {
            commandPath = new String[]{"cmd", "/c", "start \"" + programPath + "\" " + param};

        }

        try
        {
            Runtime.getRuntime().exec(commandPath);
            isProgramAbleToRun =  true;
        }
        catch(Exception e)
        {
            log.debug("Failed to run " + programPath + " " + param + ": ", e);
        }


        System.out.println(programPath + " " + param);

        try {
            if(isProgramAbleToRun)
            {
                System.out.println("thread sleep");
                Thread.sleep(500);//prevent some program failed to pop out
            }
        }
        catch(Exception e)
        {
            log.debug("Thread sleep failed: ", e);
        }

        return isProgramAbleToRun;
    }

    public static boolean isProgramPathExist(String appPath)
    {
        if(new File(appPath).exists() == false)
        {
            log.debug("Program not found - " + appPath);

            return false;
        }

        return true;
    }

    public static void failToOpenProgramPathMessage(String message)
    {
        log.info(message);
        showMessageDialog(null, message,
                "Oops!", JOptionPane.INFORMATION_MESSAGE, browserNotFoundIcon);
    }
}
