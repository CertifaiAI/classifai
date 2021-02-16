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
package ai.classifai.util.collection;

import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

/**
 * UUID Generator
 *
 * @author YCCertifai
 */
@NoArgsConstructor
public class UUIDGenerator
{
    public static String generateUUID()
    {
        String newID = UUID.randomUUID().toString();

        return newID;
    }
}