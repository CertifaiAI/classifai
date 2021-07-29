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
package ai.classifai.database.entity.generic;

import ai.classifai.core.entity.dto.generic.PointDTO;
import ai.classifai.core.entity.model.generic.Point;
import ai.classifai.database.entity.image.annotation.ImageAnnotationEntity;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

/**
 * Class for Point entity with hibernate implementation
 *
 * @author YinChuangSum
 */
@NoArgsConstructor
@lombok.Data
@Entity(name = "point")
public class PointEntity implements Point
{
    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @Column(name = "x")
    private Float x;

    @Column(name = "y")
    private Float y;

    @Column(name = "dist_2_img_x")
    private Float dist2ImgX;

    @Column(name = "dist_2_img_y")
    private Float dist2ImgY;

    @Column(name = "position")
    private Integer position;

    @ManyToOne
    @JoinColumn(name = "annotation_id")
    private ImageAnnotationEntity annotation;

    public PointDTO toDTO()
    {
        return PointDTO.builder()
                .id(id)
                .x(x)
                .y(y)
                .dist2ImgX(dist2ImgX)
                .dist2ImgY(dist2ImgY)
                .position(position)
                .build();
    }

    @Override
    public void fromDTO(PointDTO dto)
    {
        setId(dto.getId());
        update(dto);
    }

    @Override
    public void update(PointDTO dto)
    {
        setX(dto.getX());
        setY(dto.getY());
        setDist2ImgX(dto.getDist2ImgX());
        setDist2ImgY(dto.getDist2ImgY());
        setPosition(dto.getPosition());
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Point)
        {
            Point point = (Point) obj;
            return x.equals(point.getX()) && y.equals(point.getY())
            && dist2ImgX.equals(point.getDist2ImgX()) && dist2ImgY.equals(point.getDist2ImgY());
        }

        return false;
    }

}
