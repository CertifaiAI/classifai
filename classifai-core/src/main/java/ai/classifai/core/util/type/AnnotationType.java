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
package ai.classifai.core.util.type;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Locale;

/**
 * Types of Annotation
 *
 * @author codenamewei
 */
@Slf4j
public enum AnnotationType
{
    BOUNDINGBOX,
    SEGMENTATION;
    //ADD WHEN HAVE NEW ANNOTATION TYPE

    public static AnnotationType getTypeFromEndpoint(@NonNull String annotation)
    {
        AnnotationType type = null;

        if(annotation.equals("bndbox"))
        {
            type = BOUNDINGBOX;
        }
        else if(annotation.equals("seg"))
        {
            type = SEGMENTATION;
        }
        return type;
    }

    public static boolean checkSanity(@NonNull Integer annotationTypeInt)
    {
        if (annotationTypeInt.equals(BOUNDINGBOX.ordinal()) || annotationTypeInt.equals(SEGMENTATION.ordinal()))
        {
            return true;
        }
        else
        {
            log.debug("Annotation unmatched in AnnotationType. AnnotationType only accepts [boundingbox/segmentation]");
            return false;
        }
    }

    public static AnnotationType get(@NonNull String caseInsensitive)
    {
        caseInsensitive = caseInsensitive.toUpperCase(Locale.ROOT);

        if (caseInsensitive.equals(BOUNDINGBOX.name()))
        {
            return BOUNDINGBOX;
        }
        else if (caseInsensitive.equals(SEGMENTATION.name()))
        {
            return SEGMENTATION;
        }

        log.debug("Annotation type from string resulted in failure: " + caseInsensitive);

        return null;
    }

    public static AnnotationType get(@NonNull Integer ordinal)
    {
        if (ordinal.equals(BOUNDINGBOX.ordinal()))
        {
            return BOUNDINGBOX;
        }
        else if (ordinal.equals(SEGMENTATION.ordinal()))
        {
            return SEGMENTATION;
        }

        log.debug("Annotation type from integer resulted in failure: " + ordinal);

        return null;
    }
}

