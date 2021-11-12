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
package ai.classifai.backend.action;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Import of label list and pass to frontend
 *
 * @author codenamewei
 */
@Slf4j
public class LabelListImport
{
    public List<String> getValidLabelList(File labelFile) {
        if(!labelFile.exists())
        {
            return new ArrayList<>();
        }

        try
        {
            String labels = IOUtils.toString(new FileReader(labelFile));

            return Arrays.asList(getValidLabelList(labels.split("\n")));
        }
        catch(Exception e)
        {
            log.info("Error in importing label list. ", e);
            return new ArrayList<>();
        }
    }

    private String[] getValidLabelList(String[] inputLabelList)
    {
        List<String> validLabelList = new ArrayList<>();

        for(String label : inputLabelList)
        {
            String buffer = label.replaceAll("\\s", "");

            if(buffer.length() > 0)
            {
                validLabelList.add(buffer);
            }
        }

        return validLabelList.toArray(new String[0]);
    }
}
