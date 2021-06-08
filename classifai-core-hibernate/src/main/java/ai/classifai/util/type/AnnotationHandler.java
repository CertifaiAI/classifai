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

import io.vertx.jdbcclient.JDBCPool;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Check for annotation type
 *
 * Reminder to add here when have new annotation method
 *
 * @author codenamewei
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AnnotationHandler
{
    //annotationInt, jdbc
    private static Map<Integer, JDBCPool> annotationJDBCPool = new HashMap<>();

    public static boolean checkSanity(@NonNull Integer annotationTypeInt)
    {
        if (annotationTypeInt.equals(AnnotationType.BOUNDINGBOX.ordinal()) || annotationTypeInt.equals(AnnotationType.SEGMENTATION.ordinal()))
        {
            return true;
        }
        else
        {
            log.debug("Annotation unmatched in AnnotationType. AnnotationType only accepts [boundingbox/segmentation]");
            return false;
        }
    }

    public static void addJDBCPool(@NonNull AnnotationType type, @NonNull JDBCPool jdbcPool)
    {
        annotationJDBCPool.put(type.ordinal(), jdbcPool);
    }

    public static AnnotationType getTypeFromEndpoint(@NonNull String annotation)
    {
        AnnotationType type = null;

        if(annotation.equals("bndbox"))
        {
            type = AnnotationType.BOUNDINGBOX;
        }
        else if(annotation.equals("seg"))
        {
            type = AnnotationType.SEGMENTATION;
        }
        return type;
    }


    public static AnnotationType getType(@NonNull String type)
    {
        type = type.toUpperCase(Locale.ROOT);

        if (type.equals(AnnotationType.BOUNDINGBOX.name()))
        {
            return AnnotationType.BOUNDINGBOX;
        }
        else if (type.equals(AnnotationType.SEGMENTATION.name()))
        {
            return AnnotationType.SEGMENTATION;
        }

        log.debug("Annotation type from string resulted in failure: " + type);

        return null;
    }

    public static AnnotationType getType(@NonNull Integer type)
    {
        if (type.equals(AnnotationType.BOUNDINGBOX.ordinal()))
        {
            return AnnotationType.BOUNDINGBOX;
        }
        else if (type.equals(AnnotationType.SEGMENTATION.ordinal()))
        {
            return AnnotationType.SEGMENTATION;
        }

        log.debug("Annotation type from integer resulted in failure: " + type);

        return null;
    }


}