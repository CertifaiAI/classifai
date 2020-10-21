/*
 * Copyright (c) 2020 CertifAI Sdn. Bhd.
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

package ai.classifai;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * Integration test
 */
@Slf4j
public class AppTest 
{
    @Test
    public void initializeAppSuccessfully()
    {
        ai.classifai.ClassifaiApp.main(null);
    }
}
