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
package ai.classifai.loader;

import ai.classifai.util.type.AnnotationType;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * Project Initiator from command line argument
 *
 * @author codenamewei
 */
@Slf4j
@Builder
@Getter
public class CLIProjectInitiator
{
    private AnnotationType projectType;
    
    @Builder.Default private String projectName = new NameGenerator().getNewProjectName();

    @Builder.Default private File rootDataPath = null;

    @Builder.Default private File labelFilePath = null;

}