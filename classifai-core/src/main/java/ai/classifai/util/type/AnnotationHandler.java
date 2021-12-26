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

import ai.classifai.loader.ProjectLoader;
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
        if (annotationTypeInt.equals(AnnotationType.VIDEOBOUNDINGBOX.ordinal()) || annotationTypeInt.equals(AnnotationType.VIDEOSEGMENTATION.ordinal()))
        {
            return true;
        }
        else
        {
            log.debug("Annotation unmatched in AnnotationType. AnnotationType only accepts [boundingbox/segmentation/videoboundingbox/videosegmentation]");
            return false;
        }
    }

    public static void addJDBCPool(@NonNull AnnotationType type, @NonNull JDBCPool jdbcPool)
    {
        annotationJDBCPool.put(type.ordinal(), jdbcPool);
    }

    public static JDBCPool getJDBCPool(@NonNull ProjectLoader loader)
    {
        return annotationJDBCPool.get(loader.getAnnotationType());
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
        else if(annotation.equals("videobndbox"))
        {
            type = AnnotationType.VIDEOBOUNDINGBOX;
        }
        else if(annotation.equals("videoseg"))
        {
            type = AnnotationType.VIDEOSEGMENTATION;
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
        else if(type.equals(AnnotationType.VIDEOBOUNDINGBOX.name()))
        {
            return  AnnotationType.VIDEOBOUNDINGBOX;
        }
        else if(type.equals(AnnotationType.VIDEOSEGMENTATION.name()))
        {
            return  AnnotationType.VIDEOSEGMENTATION;
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
        else if (type.equals(AnnotationType.VIDEOBOUNDINGBOX.ordinal()))
        {
            return AnnotationType.VIDEOBOUNDINGBOX;
        }
        else if (type.equals(AnnotationType.VIDEOSEGMENTATION.ordinal()))
        {
            return AnnotationType.VIDEOSEGMENTATION;
        }

        log.debug("Annotation type from integer resulted in failure: " + type);

        return null;
    }


}