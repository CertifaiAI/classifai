package ai.classifai.action;

import ai.classifai.database.annotation.AnnotationQuery;
import ai.classifai.database.portfolio.PortfolioVerticle;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.collection.ConversionHandler;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.project.ProjectHandler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Slf4j
public class DeleteProjectData {

    private static ProjectLoader loader;
    private static String projectId;

    public static void deleteProjectData(JDBCPool jdbcPool, Message<JsonObject> message)
    {
        projectId =  message.body().getString(ParamConfig.getProjectIdParam());
        loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectId));

        JsonArray UUIDListJsonArray =  message.body().getJsonArray(ParamConfig.getUuidListParam());

        List<String> deleteUUIDList = ConversionHandler.jsonArray2StringList(UUIDListJsonArray);
        String uuidQueryParam = String.join(",", deleteUUIDList);

        Tuple params = Tuple.of(projectId, uuidQueryParam);

        jdbcPool.preparedQuery(AnnotationQuery.getDeleteProjectData())
                .execute(params)
                .onComplete(fetch -> {
                    if (fetch.succeeded())
                    {
                        try {
                            deleteProjectDataOnComplete(message, deleteUUIDList);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        message.replyAndRequest(ReplyHandler.reportDatabaseQueryError(fetch.cause()));
                    }
                });
    }

    private static void deleteProjectDataOnComplete(Message<JsonObject> message, List<String> deleteUUIDList) throws IOException {
        List<String> dbUUIDList = loader.getUuidListFromDb();
        JsonArray deletedDataPath = message.body().getJsonArray(ParamConfig.getImgPathListParam());
        List<String> deletedDataPathList = ConversionHandler.jsonArray2StringList(deletedDataPath);
        if (dbUUIDList.removeAll(deleteUUIDList))
        {
            loader.setUuidListFromDb(dbUUIDList);

            List<String> sanityUUIDList = loader.getSanityUuidList();

            if (sanityUUIDList.removeAll(deleteUUIDList))
            {
                loader.setSanityUuidList(sanityUUIDList);
                FileMover.moveFileToDirectory(loader.getProjectPath(), deletedDataPathList);
            }
            else
            {
                log.info("Error in removing uuid list");
            }

            //update Portfolio Verticle
            PortfolioVerticle.updateFileSystemUuidList(projectId);

            JsonObject response = ReplyHandler.getOkReply();
            response.put(ParamConfig.getUuidListParam(), loader.getSanityUuidList());
            message.replyAndRequest(response);
        }
        else
        {
            message.reply(ReplyHandler.reportUserDefinedError(
                    "Failed to remove uuid from Portfolio Verticle. Project not expected to work fine"));
        }
    }

}
