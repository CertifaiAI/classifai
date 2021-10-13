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

import ai.classifai.dto.data.ProjectConfigProperties;
import ai.classifai.loader.ProjectLoader;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Creating files for export function
 *
 * @author devenyantis
 */
@Slf4j
public class FileGenerator {

    public void run(@NonNull ProjectExport projectExport, @NonNull ProjectLoader loader, @NonNull ProjectConfigProperties configContent, @NonNull int exportType) {
        final Runnable runnable = () -> {
            String exportPath = null;
            if (exportType == ActionConfig.ExportType.CONFIG_WITH_DATA.ordinal()) {
                log.info("Exporting project config with data");
                try {
                    exportPath = projectExport.exportToFileWithData(loader, loader.getProjectId(), configContent);
                } catch (IOException e) {
                    log.info("Exporting fail");
                }
            } else if (exportType == ActionConfig.ExportType.CONFIG_ONLY.ordinal()) {
                log.info("Exporting project config");
                exportPath = projectExport.exportToFile(loader.getProjectId(), configContent);
            }

            if (exportPath != null) {
                projectExport.setExportStatus(ProjectExport.ProjectExportStatus.EXPORT_SUCCESS);
                projectExport.setExportPath(exportPath);

                return;
            }
            projectExport.setExportStatus(ProjectExport.ProjectExportStatus.EXPORT_FAIL);
        };
        new Thread(runnable, "Project Export").start();
    }
}
