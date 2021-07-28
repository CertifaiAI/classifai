package ai.classifai.service.generic;

import ai.classifai.util.Hash;
import io.vertx.core.Vertx;
import lombok.NonNull;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


public abstract class FileService extends AbstractVertxService
{
    public FileService(Vertx vertx)
    {
        super(vertx);
    }

    public Boolean isDirectory(String path)
    {
        return new File(path).isDirectory();
    }

    public Boolean isPathExists(String path)
    {
        return new File(path).exists();
    }

    public Boolean isChecksumMatch(String savedChecksum, String path)
    {
        String checksum = Hash.getHash256String(new File(path));

        return savedChecksum.equals(checksum);
    }

    protected List<File> listFileRecursively(@NonNull File rootPath)
    {
        List<File> fileList = new ArrayList<>();

        Deque<File> queue = new ArrayDeque<>();

        queue.push(rootPath);

        while (!queue.isEmpty())
        {
            File currentFolderPath = queue.pop();

            List<File> folderList = listFile(currentFolderPath);

            for (File file : folderList)
            {
                if (file.isDirectory())
                {
                    queue.push(file);
                }
                else
                {
                    fileList.add(file);
                }
            }
        }

        return fileList;
    }

    protected List<File> listFile(@NonNull File rootPath)
    {
        List<File> outputList = new ArrayList<>();

        if (rootPath.exists())
        {
            outputList = Arrays.stream(Objects.requireNonNull(rootPath.listFiles()))
                    .collect(Collectors.toList());
        }

        return outputList;
    }
}
