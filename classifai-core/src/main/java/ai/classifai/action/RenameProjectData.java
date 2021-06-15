package ai.classifai.action;

import ai.classifai.database.annotation.AnnotationQuery;
import ai.classifai.database.versioning.Annotation;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.project.ProjectHandler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class RenameProjectData {

    private static ProjectLoader loader;
    private static String dataUUID;
    private static Annotation annotation;

    public static void renameProjectData(JDBCPool jdbcPool, Message<JsonObject> message)
    {
        String projectId = message.body().getString(ParamConfig.getProjectIdParam());
        loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectId));
        dataUUID = message.body().getString(ParamConfig.getUuidParam());
        getAnnotationVersion();

        String newDataFileName = message.body().getString(ParamConfig.getNewFileNameParam());
        String oldDataFilename = getOldDataFilename();

        String updatedFilename = modifyFilenameFromCache(newDataFileName);
        File newDataPath = createNewDataPath(updatedFilename);

        if(newDataPath.exists()) {
            // Abort if name exists
            message.reply(ReplyHandler.reportUserDefinedError("Name exists"));
            return;
        }

        Tuple params = Tuple.of(updatedFilename, dataUUID, projectId);

        if(renameDataPath(newDataPath, oldDataFilename))
        {
            invokeJDBCPool(jdbcPool, message, params, newDataPath.toString());
            updateAnnotationCache(updatedFilename);
        }
        else
        {
            message.reply(ReplyHandler.reportUserDefinedError("Fail to rename file"));
        }

    }

    private static void invokeJDBCPool(JDBCPool jdbcPool, Message<JsonObject> message, Tuple params, String newDataPath)
    {
        jdbcPool.preparedQuery(AnnotationQuery.getRenameProjectData())
                .execute(params)
                .onComplete(fetch -> {
                    if(fetch.succeeded())
                    {
                        JsonObject response = ReplyHandler.getOkReply();
                        response.put(ParamConfig.getImgPathParam(), newDataPath);
                        message.replyAndRequest(response);
                    }
                    else
                    {
                        message.reply(ReplyHandler.reportUserDefinedError(
                                "Fail to update filename in database"));
                    }
                });
    }

    private static boolean renameDataPath(File newDataPath, String oldDataFilename)
    {
        File oldDataPath = new File(oldDataFilename);

        log.debug("Rename file:\nFrom: " + oldDataPath + "\nTo: " + newDataPath);

        return oldDataPath.renameTo(newDataPath);
    }

    private static String getOldDataFilename()
    {
        return Paths.get(loader.getProjectPath().toString(), annotation.getImgPath()).toString();
    }

    private static String modifyFilenameFromCache(String newFilename)
    {
        String oldDataPath = annotation.getImgPath();
        // get only the filename after last slash before file extension
        String oldDataPathFName = oldDataPath.substring(oldDataPath.lastIndexOf(File.separator) + 1, oldDataPath.lastIndexOf("."));

        String newDataPathFName = newFilename.substring(newFilename.lastIndexOf(File.separator) + 1, newFilename.lastIndexOf("."));

        // Rename old data path filename
        String newDataPathModified = oldDataPath.replace(oldDataPathFName, newDataPathFName);
        log.debug("New modified path: " + newDataPathModified);

        return newDataPathModified;
    }

    private static File createNewDataPath(String newDataFileName)
    {
        File newDataPath = Paths.get(
                loader.getProjectPath().toString(), newDataFileName).toFile();
        log.debug("New data path: " + newDataPath);

        return newDataPath;
    }

    private static void getAnnotationVersion()
    {
        Map<String, Annotation> uuidAnnotationDict = loader.getUuidAnnotationDict();
        annotation = uuidAnnotationDict.get(dataUUID);
    }

    private static void updateAnnotationCache(String newImagePath)
    {
        Map<String, Annotation> uuidAnnotationDict = loader.getUuidAnnotationDict();

        annotation.setImgPath(newImagePath);
        uuidAnnotationDict.put(dataUUID, annotation);

        loader.setUuidAnnotationDict(uuidAnnotationDict);

    }
}
