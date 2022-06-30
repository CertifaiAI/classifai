/*
 * Copyright (c) 2020-2021 CertifAI Sdn. Bhd.
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
package ai.classifai.backend.repository.database.migration;

import ai.classifai.core.utility.DbConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;

/***
 *Archive Handler
 *
 *@author YCCertifai
 */
@Slf4j
public class ArchiveHandler {

    private ArchiveHandler(){
        throw new IllegalStateException("Utility class");
    }

    private static final String ARCHIVE = ".archive";
    private static final String ARCHIVE_PATH;

    static
    {
        ARCHIVE_PATH = DbConfig.getDbRootPath() + File.separator + ARCHIVE;

        createArchiveFolder();
    }

    private static void createArchiveFolder()
    {
        File file = new File(ARCHIVE_PATH);

        if (!file.exists()) file.mkdir();
    }

    public static void copyToArchive(String path)
    {
        try
        {
            File source = new File(path);
            File destination = new File(ARCHIVE_PATH, source.getName());

            if (source.isDirectory())
            {
                FileUtils.copyDirectory(source, destination);
            }
            else
            {
                FileUtils.copyFile(source, destination);
            }
        }
        catch (Exception e)
        {
            log.error("Unable to copy " + ARCHIVE_PATH + "\n"+ e);
        }
    }
}
