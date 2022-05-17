package ai.classifai.core.application;

import com.fasterxml.jackson.databind.util.JSONPObject;

import java.io.InputStream;

public interface DataSourceService {
    InputStream getDataSource(String key);

    void saveDataSource(String key, JSONPObject dataStream);

    void deleteDataSource(String key);
}
