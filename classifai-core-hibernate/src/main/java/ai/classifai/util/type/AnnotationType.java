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

import java.util.Locale;

/**
 * Types of Annotation
 *
 * @author codenamewei
 */
public enum AnnotationType
{
    BOUNDINGBOX("bnd_box"),
    SEGMENTATION("polygon");
    //ADD WHEN HAVE NEW ANNOTATION TYPE

    public final String META_KEY;

    AnnotationType(String metaKey)
    {
        META_KEY = metaKey;
    }

    public static AnnotationType fromInt(int idx)
    {
        return AnnotationType.values()[idx];
    }

    public static AnnotationType fromString(String str)
    {
        return AnnotationType.valueOf(str.toUpperCase(Locale.ROOT));
    }
}

