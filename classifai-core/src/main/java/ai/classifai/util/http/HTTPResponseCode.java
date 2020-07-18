/*
 * Copyright (c) 2020 CertifAI
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

package ai.classifai.util.http;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP Status Code for different scenarios
 *
 * @author Chiawei Lim
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Status">httpStatusCode</a>
 */
public class HTTPResponseCode{

    private static Map<String, Integer> STATUS_CODE;

    static {
        STATUS_CODE = new HashMap<>();

        STATUS_CODE.put("OK", 200);
        STATUS_CODE.put("OK_NO_CONTENT", 204);
        STATUS_CODE.put("BAD_REQUEST", 400);

        STATUS_CODE.put("METHODS_NOT_ALLOWED", 405);
        STATUS_CODE.put("INTERNAL_SERVER_ERROR", 500);
    }

    public static Integer ok()
    {
        return STATUS_CODE.get("OK");
    }

    public static Integer badRequest()
    {
        return STATUS_CODE.get("BAD_REQUEST");
    }

    public static Integer internalServerError()
    {
        return STATUS_CODE.get("INTERNAL_SERVER_ERROR");
    }

    public static Integer methodsNotAllowed()
    {
        return STATUS_CODE.get("METHODS_NOT_ALLOWED");
    }
}
