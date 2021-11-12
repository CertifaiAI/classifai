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
package ai.classifai.core.util;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Hash-256
 *
 * @author codenamewei
 */
@Slf4j
public class Hash
{
    public static String getHash256String(@NonNull File filePath) {
        byte[] buffer= new byte[8192];

        int count;
        if(filePath.exists()) {
            try {
                //not thread safe, hence single instance per function
                MessageDigest digest = MessageDigest.getInstance("SHA-256");

                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath));

                while ((count = bis.read(buffer)) > 0)
                {
                    digest.update(buffer, 0, count);
                }

                bis.close();

                byte[] hash = digest.digest();

                return Base64.getEncoder().encodeToString(hash);
            } catch (Exception e) {
                log.info("Fail to get hash", e);
            }
        }
        log.info("File not found: " + filePath);

        return null;
    }
}