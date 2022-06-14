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
package ai.classifai.backend.data.loader;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.SecureRandom;


/**
 * Random Name Generator when using cli and create a project without default project name
 *
 * @author codenamewei
 */
@Slf4j
public class NameGenerator
{
    private JSONObject jsonObject = null;

    public NameGenerator()
    {
        try
        {
            //classifai-core/src/main/resources/file-name which store the name combinations
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("name.json");

            if (inputStream == null)
            {
                throw new FileNotFoundException("File for name generator not found.");
            }

            JSONParser parser = new JSONParser();

            Object obj = parser.parse(new InputStreamReader(inputStream));

            jsonObject = (JSONObject) obj;
        }
        catch (Exception e)
        {
            log.info("Create new project from cli might failed. ", e);
        }
    }

    public String getNewProjectName()
    {
        if (jsonObject == null)
        {
            return "default";
        }

        return randomPick("left") + "_" + randomPick("right");
    }


    private String randomPick(String key)
    {
        JSONArray jsonArray = (JSONArray) jsonObject.get(key);

        int selectedIndex = new SecureRandom().nextInt(jsonArray.size());

        return (String) jsonArray.get(selectedIndex);
    }
}
