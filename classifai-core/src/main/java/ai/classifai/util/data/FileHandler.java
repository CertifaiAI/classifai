package ai.classifai.util.data;

import ai.classifai.util.ParamConfig;
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

    public static String getAbsolutePath(@NonNull File filePath)
    {
        String fullPath = filePath.getAbsolutePath();

        String[] subString = fullPath.split(ParamConfig.getFileSeparator());

        String fileNameWithExtension = subString[subString.length - 1];

        int fileStartIndex = fullPath.length() - fileNameWithExtension.length();

        return fullPath.substring(0, fileStartIndex);
    }

    public static String getFileName(@NonNull String filePath)
    {
        String[] subString = filePath.split(ParamConfig.getFileSeparator());

        String fileNameWithExtension = subString[subString.length - 1];

        String[] separator = fileNameWithExtension.split("\\.");

        int fileEndIndex = filePath.length() -  separator[(separator.length - 1)].length() - 1;
        int fileStartIndex = filePath.length() - fileNameWithExtension.length();

        String fileName = filePath.substring(fileStartIndex, fileEndIndex);

        return fileName;
    }
}
