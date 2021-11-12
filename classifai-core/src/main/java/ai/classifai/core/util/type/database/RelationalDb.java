package ai.classifai.core.util.type.database;

import lombok.Getter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public abstract class RelationalDb
{
    protected String driver;
    protected String dbFileExtension;
    protected String lckFileExtension;

    protected String urlHeader;
    protected String user;
    protected String password;

    protected Map<String, File> tableAbsPathDict;
    protected List<File> lockAbsPathList;

    public void setupDb(List<String> tableKey, Map<String, String> tableFullPathList)
    {
        tableAbsPathDict = new HashMap<>();
        lockAbsPathList = new ArrayList<>();

        for(String key : tableKey)
        {
            String tableAbsPath = tableFullPathList.get(key) + dbFileExtension;
            String lockFullPath = tableFullPathList.get(key)  + lckFileExtension;

            tableAbsPathDict.put(key, new File(tableAbsPath));
            lockAbsPathList.add(new File(lockFullPath));
        }
    }

    public boolean isDbExist()
    {
        for(File tablePath : tableAbsPathDict.values())
        {
            if(tablePath.exists())
            {
                return true;
            }
        }

        return false;
    }
}
