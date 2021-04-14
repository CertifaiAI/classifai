/*
 * Copyright (c) 2021 CertifAI Sdn. Bhd.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.classifai.action;

import ai.classifai.action.parser.PortfolioParser;
import ai.classifai.action.parser.ProjectParser;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.data.ImageHandler;
import ai.classifai.util.datetime.DateTime;
import ai.classifai.util.project.ProjectHandler;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Export of project to a configuration file
 *
 * @author codenamewei
 */
@Builder
@Slf4j
@NoArgsConstructor
public class ProjectExport
{
    public static JsonObject getConfigSkeletonStructure()
    {
        return new JsonObject()
                .put(ActionConfig.getToolParam(), ActionConfig.getToolName())
                .put(ActionConfig.getToolVersionParam(), ActionConfig.getToolVersion())
                .put(ActionConfig.getUpdatedDateParam(), new DateTime().toString());
    }

    private static String exportToFile(@NonNull String projectId, @NonNull JsonObject jsonObject)
    {
        ProjectLoader loader = ProjectHandler.getProjectLoader(projectId);

        //Configuration file of json format
        String configPath = loader.getProjectPath() + File.separator + loader.getProjectName() + ".json";

        try
        {
            FileWriter file = new FileWriter(configPath);

            file.write(jsonObject.encodePrettily());

            file.close();

            log.info("Project configuration file saved at: " + configPath);
        }
        catch (IOException e)
        {
            return "Path cannot be provided due to failed configuration: " + e;
        }

        return configPath;
    }

    private static String exportToFileWithData(ProjectLoader loader, String projectId, JsonObject configContent) throws IOException
    {
        String configPath = exportToFile(projectId, configContent);
        File zipFile = Paths.get(loader.getProjectPath(), loader.getProjectName() + ".zip").toFile();
        List<File> validImagePaths = ImageHandler.getValidImagesFromFolder(new File(loader.getProjectPath()));

        FileOutputStream fos = new FileOutputStream(zipFile);
        ZipOutputStream out = new ZipOutputStream(fos);

        // Add config file
        addToEntry(new File(configPath), out, new File(loader.getProjectPath()));

        // Add all image data
        for(File filePath: validImagePaths)
        {
            addToEntry(filePath, out, new File(loader.getProjectPath()));
        }
        out.close();
        fos.close();

        log.info("Project configuration file and data saved at: " + zipFile);

        return zipFile.toString();
    }

    private static void addToEntry(File filePath, ZipOutputStream out, File dir) throws IOException
    {
        String relativePath = filePath.toString().substring(dir.getAbsolutePath().length()+1);
        String saveFileRelativePath = Paths.get(filePath.getParentFile().getName(), relativePath).toFile().toString();

        ZipEntry entry = new ZipEntry(saveFileRelativePath);
        out.putNextEntry(entry);

        try(FileInputStream fis = new FileInputStream(filePath))
        {
            byte[] buffer = new byte[1024];
            int len;

            while((len = fis.read(buffer)) > 0)
            {
                out.write(buffer, 0, len);
            }

            out.closeEntry();
        }
        catch (Exception e)
        {
            log.debug(e.toString());
        }
    }

    public static String runExportProcess(ProjectLoader loader, JsonObject configContent, int exportType)
    {
        if(exportType == ActionConfig.ExportType.CONFIG_WITH_DATA.ordinal())
        {
            log.info("Exporting Config with data");
            try {
                return exportToFileWithData(loader, loader.getProjectId(), configContent);
            } catch (IOException e) {
                log.warn("Error creating zip file");
            }
        }
        else if(exportType == ActionConfig.ExportType.CONFIG_ONLY.ordinal())
        {
            log.info("Exporting Config only");
            return exportToFile(loader.getProjectId(), configContent);
        }

        return null;
    }

    public static JsonObject getConfigContent(@NonNull RowSet<Row> rowSet, @NonNull RowSet<Row> projectRowSet)
    {
        if(rowSet.size() == 0)
        {
            log.debug("Export project retrieve 0 rows. Project not found from portfolio database");
            return null;
        }

        JsonObject configContent = getConfigSkeletonStructure();
        PortfolioParser.parseOut(rowSet.iterator().next(), configContent);

        if(projectRowSet.size() == 0)
        {
            log.debug("Export project annotation retrieve 0 rows. Project not found from project database");
            return null;
        }

        ProjectParser.parseOut(
                configContent.getString(ParamConfig.getProjectPathParam()),
                projectRowSet.iterator(), configContent);

        return configContent;
    }

    public static ActionConfig.ExportType getExportType(String exportType)
    {
        if(exportType.equals("cfg"))
        {
            return ActionConfig.ExportType.CONFIG_ONLY;
        }
        else if(exportType.equals("cfgdata"))
        {
            return ActionConfig.ExportType.CONFIG_WITH_DATA;
        }
        return ActionConfig.ExportType.INVALID_CONFIG;
    }
}
