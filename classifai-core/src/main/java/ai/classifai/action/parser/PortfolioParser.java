package ai.classifai.action.parser;

import ai.classifai.database.annotation.bndbox.BoundingBoxVerticle;
import ai.classifai.database.annotation.seg.SegVerticle;
import ai.classifai.loader.LoaderStatus;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.ProjectHandler;
import ai.classifai.util.data.StringHandler;
import ai.classifai.util.type.AnnotationType;
import ai.classifai.util.versioning.ProjectVersion;
import ai.classifai.util.versioning.VersionCollection;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;


/***
 * Parsing project in and out classifai with configuration file
 *
 * @author codenamewei
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PortfolioParser
{
    public static void parseOut(@NonNull Row inputRow, @NonNull JsonObject jsonObject)
    {
        jsonObject.put(ParamConfig.getProjectIdParam(), inputRow.getString(0));
        jsonObject.put(ParamConfig.getProjectNameParam(), inputRow.getString(1));
        jsonObject.put(ParamConfig.getAnnotationTypeParam(), inputRow.getInteger(2));

        jsonObject.put(ParamConfig.getProjectPathParam(), inputRow.getString(3));
        jsonObject.put(ParamConfig.getIsNewParam(), inputRow.getBoolean(4));
        jsonObject.put(ParamConfig.getIsStarredParam(), inputRow.getBoolean(5));

        jsonObject.put(ParamConfig.getCurrentVersionUuidParam(), inputRow.getString(6));
        jsonObject.put(ParamConfig.getVersionListParam(), StringHandler.cleanUpRegex(inputRow.getString(7), Arrays.asList("\"")));
        jsonObject.put(ParamConfig.getUuidVersionListParam(), StringHandler.cleanUpRegex(inputRow.getString(8), Arrays.asList("\"")));
        jsonObject.put(ParamConfig.getLabelVersionListParam(), StringHandler.cleanUpRegex(inputRow.getString(9), Arrays.asList("\"")));
    }

    public static void parseIn(@NonNull JsonObject jsonObject)
    {
        VersionCollection versionCollector = new VersionCollection(jsonObject.getString(ParamConfig.getVersionListParam()));
        ProjectVersion projVersion = versionCollector.getVersionUuidDict().get(jsonObject.getString(ParamConfig.getCurrentVersionUuidParam()));

        versionCollector.setUuidDict(jsonObject.getString(ParamConfig.getUuidVersionListParam()));
        versionCollector.setLabelDict(jsonObject.getString(ParamConfig.getLabelVersionListParam()));

        ProjectLoader loader = new ProjectLoader.Builder()
                                .projectID(jsonObject.getString(ParamConfig.getProjectIdParam()))
                                .projectName(jsonObject.getString(ParamConfig.getProjectNameParam()))
                                .annotationType(jsonObject.getInteger(ParamConfig.getAnnotationTypeParam()))

                                .projectPath(jsonObject.getString(ParamConfig.getProjectPathParam()))
                                .isProjectNew(jsonObject.getBoolean(ParamConfig.getIsNewParam()))
                                .isProjectStarred(jsonObject.getBoolean(ParamConfig.getIsStarredParam()))

                                .loaderStatus(LoaderStatus.DID_NOT_INITIATED)

                                .currentProjectVersion(projVersion)
                                .versionCollection(versionCollector)

                                .build();

        ProjectHandler.loadProjectLoader(loader);

        //Check sanity of uuidList
        loader.setLoaderStatus(LoaderStatus.LOADING);

        if(loader.getAnnotationType().equals(AnnotationType.BOUNDINGBOX.ordinal()))
        {
            BoundingBoxVerticle.loadValidProjectUuid(loader.getProjectID());
        }
        else
        {
            SegVerticle.loadValidProjectUuid(loader.getProjectID());
        }

        log.info("Import project " + loader.getProjectName() + " success!");
    }
}
