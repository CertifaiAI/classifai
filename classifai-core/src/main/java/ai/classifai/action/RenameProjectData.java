package ai.classifai.action;

import ai.classifai.database.annotation.AnnotationQuery;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@Slf4j
public class RenameProjectData {

    private static ProjectLoader loader;

    public static void renameProjectData(JDBCPool jdbcPool, Message<JsonObject> message)
    {
        String projectId = message.body().getString(ParamConfig.getProjectIdParam());
        loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectId));

        String dataUUID = message.body().getString(ParamConfig.getUuidParam());
        String newDataFileName = message.body().getString(ParamConfig.getNewFileNameParam());
        String oldDataFilename = message.body().getString(ParamConfig.getImgPathParam());

        File newDataPath = createNewDataPath(newDataFileName);

        if(newDataPath.exists()) {
            // Abort if name exists
            message.reply(ReplyHandler.reportUserDefinedError("Name exists"));
            return;
        }

        Tuple params = Tuple.of(newDataFileName, dataUUID, projectId);

        if(renameDataPath(newDataPath, oldDataFilename))
        {
            invokeJDBCPool(jdbcPool, message, params);
        }
        else
        {
            message.reply(ReplyHandler.reportUserDefinedError("Fail to rename file"));
        }

    }

    private static void invokeJDBCPool(JDBCPool jdbcPool, Message<JsonObject> message, Tuple params)
    {
        jdbcPool.preparedQuery(AnnotationQuery.getRenameProjectData())
                .execute(params)
                .onComplete(fetch -> {
                    if(fetch.succeeded())
                    {
                        message.replyAndRequest(ReplyHandler.getOkReply());
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

    private static File createNewDataPath(String newDataFileName)
    {
        File newDataPath = Paths.get(
                loader.getProjectPath().toString(), newDataFileName).toFile();
        log.debug("New data path: " + newDataPath);

        return newDataPath;
    }

    private static void updateProjectLoader
}
