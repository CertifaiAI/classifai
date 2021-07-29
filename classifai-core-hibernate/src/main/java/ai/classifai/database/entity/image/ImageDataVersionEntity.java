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
package ai.classifai.database.entity.image;

import ai.classifai.core.entity.dto.generic.AnnotationDTO;
import ai.classifai.core.entity.dto.generic.DataVersionDTO;
import ai.classifai.core.entity.dto.image.ImageDataDTO;
import ai.classifai.core.entity.dto.image.ImageDataVersionDTO;
import ai.classifai.core.entity.model.generic.Annotation;
import ai.classifai.core.entity.model.image.ImageData;
import ai.classifai.core.entity.model.image.ImageDataVersion;
import ai.classifai.database.entity.generic.DataVersionEntity;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.List;

/**
 * Class for ImageDataVersion entity with hibernate implementation
 *
 * @author YinChuangSum
 */
@EqualsAndHashCode(callSuper = true)
@lombok.Data
@Entity(name = "image_data_version")
public class ImageDataVersionEntity extends DataVersionEntity implements ImageDataVersion
{
    @Column(name = "img_x")
    private Float imgX;

    @Column(name = "img_y")
    private Float imgY;

    @Column(name = "img_w")
    private Float imgW;

    @Column(name = "img_h")
    private Float imgH;

    public ImageDataVersionEntity()
    {
        super();
    }

    @Override
    public ImageDataVersionDTO toDTO()
    {
        return ImageDataVersionDTO.builder()
                .dataId(getId().getDataId())
                .versionId(getId().getVersionId())
                .imgX(imgX)
                .imgY(imgY)
                .imgH(imgH)
                .imgW(imgW)
                .build();
    }

    @Override
    public void fromDTO(DataVersionDTO dataVersionDTO)
    {
        ImageDataVersionDTO dto = ImageDataVersionDTO.toDTOImpl(dataVersionDTO);
        update(dto);
    }

    @Override
    public void update(DataVersionDTO dataVersionDTO)
    {
        ImageDataVersionDTO dto = ImageDataVersionDTO.toDTOImpl(dataVersionDTO);
        setImgX(dto.getImgX());
        setImgY(dto.getImgY());
        setImgW(dto.getImgW());
        setImgH(dto.getImgH());
    }
}
