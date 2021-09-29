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
import ai.classifai.dto.data.ImageDataProperties;
import ai.classifai.dto.data.ProjectConfigProperties;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.util.data.ImageHandler;
import ai.classifai.util.datetime.DateTime;
import ai.classifai.util.project.ProjectHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    public enum ProjectExportStatus {
        EXPORT_NOT_INITIATED,
        EXPORT_STARTING,
        EXPORT_SUCCESS,
        EXPORT_FAIL
    }

    @Getter @Setter
    private static ProjectExportStatus exportStatus = ProjectExportStatus.EXPORT_NOT_INITIATED;
    @Getter @Setter
    private static String exportPath = "";

    public static String exportToFile(@NonNull String projectId, @NonNull ProjectConfigProperties configProperties)
    {
        ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectId));

        //Configuration file of json format
        String configPath = loader.getProjectPath() + File.separator + loader.getProjectName() + ".json";

        try
        {
            FileWriter file = new FileWriter(configPath);

            ObjectMapper mp = new ObjectMapper();
            String jsonString = mp.writerWithDefaultPrettyPrinter().writeValueAsString(configProperties);

            file.write(jsonString);

            file.close();

            log.info("Project configuration file saved at: " + configPath);
        }
        catch (IOException e)
        {
            return "Path cannot be provided due to failed configuration: " + e;
        }

        return configPath;
    }

    public static String exportToFileWithData(ProjectLoader loader, String projectId, ProjectConfigProperties configContent) throws IOException
    {
        String configPath = exportToFile(projectId, configContent);
        File zipFile = Paths.get(loader.getProjectPath().getAbsolutePath(), loader.getProjectName() + ".zip").toFile();
        List<String> validImagePaths = ImageHandler.getValidImagesFromFolder(loader.getProjectPath());

        FileOutputStream fos = new FileOutputStream(zipFile);
        ZipOutputStream out = new ZipOutputStream(fos);

        // Add config file
        addToEntry(new File(configPath), out, loader.getProjectPath());

        // Add all data
        validImagePaths.forEach(
                s -> {
                    try {
                        addToEntry(new File(s), out, loader.getProjectPath());
                    } catch (IOException e) {
                        log.info("Fail to add data into zip file");
                    }
                }
        );
        out.close();
        fos.close();

        log.info("Project configuration file and data saved at: " + zipFile);

        return zipFile.toString();
    }

    private static void addToEntry(File filePath, ZipOutputStream out, File dir) throws IOException
    {
        String relativePath = filePath.toString().substring(dir.getAbsolutePath().length()+1);
        String saveFileRelativePath = Paths.get(dir.getName(), relativePath).toFile().toString();

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

    public static ProjectConfigProperties getConfigContent(@NonNull RowSet<Row> rowSet, @NonNull RowSet<Row> projectRowSet)
    {
        if(rowSet.size() == 0)
        {
            log.debug("Export project retrieve 0 rows. Project not found from portfolio database");
            return null;
        }

        ProjectConfigProperties projectConfig = PortfolioParser.parseOut(rowSet.iterator().next());
        projectConfig.setToolName(ActionConfig.getToolName());
        projectConfig.setToolVersion(ActionConfig.getToolVersion());
        projectConfig.setUpdateDate(new DateTime().toString());

        if(projectRowSet.size() == 0)
        {
            log.debug("Export project annotation retrieve 0 rows. Project not found from project database");
            return null;
        }

        Map<String, ImageDataProperties> configProperties =  ProjectParser.parseOut(
                projectConfig.getProjectPath(), projectRowSet.iterator());

        projectConfig.setContent(configProperties);

        return projectConfig;
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
