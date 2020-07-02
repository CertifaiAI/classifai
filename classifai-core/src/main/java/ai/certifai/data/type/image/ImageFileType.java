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

package ai.certifai.data.type.image;

public class ImageFileType
{
    private static final String[] ALLOWED_FILE_TYPES = new String[]{"jpg", "png", "jpeg", "pdf", "JPG", "PNG", "JPEG"};

    public static String[] getImageFileTypes()
    {
        return ALLOWED_FILE_TYPES;
    }

}
