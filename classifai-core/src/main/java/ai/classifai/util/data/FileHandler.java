package ai.classifai.util.data;

import lombok.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class FileHandler
{
    public static List<File> processFolder(@NonNull File rootPath, @NonNull String[] extensionFormat)
    {
        List<File> totalFilelist = new ArrayList<>();

        Stack<File> folderStack = new Stack<>();

        folderStack.push(rootPath);

        while(folderStack.isEmpty() != true)
        {
            File currentFolderPath = folderStack.pop();

            File[] folderList = currentFolderPath.listFiles();

            for(File file : folderList)
            {
                if (file.isDirectory())
                {
                    folderStack.push(file);
                }
                else
                {
                    if(isfileSupported(file.getAbsolutePath(), extensionFormat))
                    {
                        totalFilelist.add(file);
                    }
                }
            }
        }

        return totalFilelist;
    }

    public static boolean isfileSupported(String file, String[] formatTypes)
    {
        for(String format : formatTypes)
        {
            Integer beginIndex = file.length() - format.length();
            Integer endIndex = file.length();

            if(file.substring(beginIndex, endIndex).equals(format))
            {
                return true;
            }
        }
        return false;
    }
}
