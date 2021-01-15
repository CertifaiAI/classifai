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

import lombok.extern.slf4j.Slf4j;

import java.util.Locale;

/**
 * Check for annotation type
 *
 * @author codenamewei
 */
@Slf4j
public class AnnotationHandler
{
    public static boolean checkSanity(Integer annotationTypeInt)
    {
        if(annotationTypeInt.equals(AnnotationType.BOUNDINGBOX.ordinal()))
        {
            return true;
        }
        else if(annotationTypeInt.equals(AnnotationType.SEGMENTATION.ordinal()))
        {
            return true;
        }
        //TODO: ADD WHEN HAVE NEW ANNOTATION TYPE
        else
        {
            printFailedMessage();
            return false;
        }
    }


    public static boolean checkSanity(String annotationType)
    {
        if(annotationType.equals(AnnotationType.BOUNDINGBOX.name()))
        {
            return true;
        }
        else if(annotationType.equals(AnnotationType.SEGMENTATION.name()))
        {
            return true;
        }
        //TODO: ADD WHEN HAVE NEW ANNOTATION TYPE
        else
        {
            printFailedMessage();
            return false;
        }
    }

    public static AnnotationType getType(String type)
    {
        type = type.toUpperCase(Locale.ROOT);

        if(type.equals(AnnotationType.BOUNDINGBOX.name()))
        {
            return AnnotationType.BOUNDINGBOX;
        }
        else if(type.equals(AnnotationType.SEGMENTATION.name()))
        {
            return AnnotationType.SEGMENTATION;
        }

        return null;
    }

    public static AnnotationType getType(Integer type)
    {
        if(type.equals(AnnotationType.BOUNDINGBOX.ordinal()))
        {
            return AnnotationType.BOUNDINGBOX;
        }
        else if(type.equals(AnnotationType.SEGMENTATION.ordinal()))
        {
            return AnnotationType.SEGMENTATION;
        }

        return null;
    }


    private static void printFailedMessage()
    {
        log.debug("Annotation unmatched in AnnotationType. AnnotationType only accepts [boundingbox/segmentation]");
    }

}
