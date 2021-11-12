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
package ai.classifai.core.util.collection;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Handling list operation
 *
 * @author codenamewei
 */
public class ListHandler
{
    public static <T> List<T> convertListToUniqueList(List<T> list)
    {
        // create an empty set
        Set<T> set = new HashSet<>();

        for (T t : list)
            set.add(t);

        return ConversionHandler.set2List(set);
    }
}
