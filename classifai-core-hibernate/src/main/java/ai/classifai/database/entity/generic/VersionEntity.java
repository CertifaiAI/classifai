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

import ai.classifai.core.entity.dto.generic.VersionDTO;
import ai.classifai.core.entity.model.generic.Label;
import ai.classifai.core.entity.model.generic.Version;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Class for Version entity with hibernate implementation
 *
 * @author YinChuangSum
 */
@lombok.Data
@Entity(name = "version")
public class VersionEntity implements Version
{
    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @OneToMany(mappedBy = "version",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<LabelEntity> labelList;

    @ManyToOne
    @JoinColumn(name="project_id")
    private ProjectEntity project;

    @OneToMany(mappedBy = "version",
            cascade = CascadeType.ALL)
    private List<DataVersionEntity> dataVersionList;

    public VersionEntity()
    {
        labelList = new ArrayList<>();
        dataVersionList = new ArrayList<>();
    }

    @Override
    public List<Label> getLabelList()
    {
        return new ArrayList<>(labelList);
    }

    @Override
    public VersionDTO toDTO()
    {
        return VersionDTO.builder()
                .id(id)
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .labelIdList(labelList.stream()
                        .map(Label::getId)
                        .collect(Collectors.toList()))
                .projectId(project.getId())
                .build();
    }

    @Override
    public void fromDTO(VersionDTO dto)
    {
        update(dto);
    }

    @Override
    public void update(VersionDTO dto)
    {
        setCreatedAt(dto.getCreatedAt());
        setModifiedAt(dto.getModifiedAt());
    }

    public void addLabel(LabelEntity label)
    {
        label.setVersion(this);
        labelList.add(label);
    }

    public void addDataVersion(DataVersionEntity dataVersion)
    {
        dataVersion.setVersion(this);
        dataVersionList.add(dataVersion);
    }
}
