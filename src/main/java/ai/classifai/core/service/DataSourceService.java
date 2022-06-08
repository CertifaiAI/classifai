package ai.classifai.core.service;

import com.fasterxml.jackson.databind.util.JSONPObject;

import java.io.InputStream;

public interface DataSourceService {
    InputStream getDataSource(String key) throws Exception;

    void saveDataSource(String key, JSONPObject dataStream) throws Exception;

    void deleteDataSource(String key) throws Exception;
}
