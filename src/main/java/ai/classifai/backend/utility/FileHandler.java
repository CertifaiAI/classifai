package ai.classifai.backend.utility;

import lombok.NonNull;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FileHandler {
    public static List<String> processFolder(@NonNull File rootPath, @NonNull Predicate<File> filterFunction)
    {
        List<String> totalFilelist = new ArrayList<>();

        Deque<File> queue = new ArrayDeque<>();

        queue.push(rootPath);

        while (!queue.isEmpty())
        {
            File currentFolderPath = queue.pop();

            List<File> folderList = listFiles(currentFolderPath);

            for (File file : folderList)
            {
                if (file.isDirectory())
                {
                    queue.push(file);
                }
                else
                {
                    if (filterFunction.test(file))
                    {
                        totalFilelist.add(file.getAbsolutePath());
                    }
                }
            }
        }

        return totalFilelist;
    }

    private static FilenameFilter getDeletedImageFolderFilter()
    {
        return (dir, name) -> !name.equals(".classifai_deleted_data");
    }

    private static List<File> listFiles(File rootPath)
    {
        List<File> outputList = new ArrayList<>();

        if (rootPath.exists())
        {
            outputList = Arrays.stream(Objects.requireNonNull(rootPath.listFiles(getDeletedImageFolderFilter())))
                    .collect(Collectors.toList());
        }

        return outputList;
    }
}
