/*
 * Copyright (c) 2021 CertifAI Sdn. Bhd.
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
package ai.classifai.database.entity.image.annotation;

import ai.classifai.core.entity.model.generic.Point;
import ai.classifai.core.entity.model.image.annotation.ImageAnnotation;
import ai.classifai.database.entity.generic.AnnotationEntity;
import ai.classifai.database.entity.generic.PointEntity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for ImageAnnotation entity with hibernate implementation
 *
 * @author YinChuangSum
 */
@Entity(name = "image_annotation")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ImageAnnotationEntity extends AnnotationEntity implements ImageAnnotation
{
    protected ImageAnnotationEntity()
    {
        pointList = new ArrayList<>();
    }

    @OneToMany(mappedBy = "annotation",
            cascade = CascadeType.ALL)
    List<PointEntity> pointList;

    @Override
    public List<Point> getPointList()
    {
        return new ArrayList<>(pointList);
    }

    public void addPoint(PointEntity point)
    {
        point.setAnnotation(this);
        pointList.add(point);
    }
}
