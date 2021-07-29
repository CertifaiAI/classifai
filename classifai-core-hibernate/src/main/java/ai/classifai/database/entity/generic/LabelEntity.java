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

import ai.classifai.core.entity.dto.generic.LabelDTO;
import ai.classifai.core.entity.model.generic.Label;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

/**
 * Class for Label entity with hibernate implementation
 *
 * @author YinChuangSum
 */
@NoArgsConstructor
@lombok.Data
@Entity(name = "label")
public class LabelEntity implements Label
{
    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "color")
    private String color;

    @ManyToOne
    @JoinColumn(name = "version_id")
    private VersionEntity version;

    public LabelDTO toDTO()
    {
        return LabelDTO.builder()
                .id(id)
                .name(name)
                .versionId(version.getId())
                .color(color)
                .build();
    }

    @Override
    public void fromDTO(LabelDTO dto)
    {
        setId(dto.getId());
        update(dto);
    }

    @Override
    public void update(LabelDTO dto)
    {
        setColor(dto.getColor());
        setName(dto.getName());
    }
}
