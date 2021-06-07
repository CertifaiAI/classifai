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
package ai.classifai.util.type;


/**
 * Image Format Supported
 *
 * @author codenamewei
 */
public enum FileFormat
{

    jpg("jpg"),
    JPG("JPG"),
    JPEG("JPEG"),
    jpeg("jpeg"),
    png("png"),
    PNG("PNG"),
    BMP("bmp"),
    PDF("pdf"),
    TIF("tif"),
    TIFF("tiff");


    private String text;

    FileFormat(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public String getUpperCase()
    {
        return text.toUpperCase();
    }
}
