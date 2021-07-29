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

import ai.classifai.core.entity.dto.generic.DataDTO;
import ai.classifai.core.entity.dto.generic.ProjectDTO;
import ai.classifai.core.entity.model.generic.Project;
import ai.classifai.core.entity.model.generic.Version;
import ai.classifai.core.entity.model.generic.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Class for Project entity with hibernate implementation
 *
 * @author YinChuangSum
 */
@lombok.Data
@Entity(name = "project")
@NamedQueries({
        @NamedQuery(name = "Project.listByAnnotation",
        query = "select p from project p where p.type = :type"),
        @NamedQuery(name = "Project.findByAnnotationAndName",
        query = "select p from project p where p.type = :type and p.name = :name")
})
public class ProjectEntity implements Project
{
    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "type")
    private Integer type;

    @Column(name = "path")
    private String path;

    @Column(name = "is_new")
    private Boolean isNew;

    @Column(name = "is_starred")
    private Boolean isStarred;

    @Column(name = "infra")
    private Integer infra;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "current_version_id")
    private VersionEntity currentVersion;

    // field name in Version class
    @OneToMany(mappedBy = "project",
            cascade = CascadeType.ALL)
    private List<VersionEntity> versionList;

    // field name in Data class
    @OneToMany(mappedBy = "project",
            cascade = CascadeType.ALL)
    private List<DataEntity> dataList;

    @Override
    public Boolean isNew()
    {
        return getIsNew();
    }

    @Override
    public Boolean isStarred()
    {
        return getIsStarred();
    }

    public ProjectEntity()
    {
        versionList = new ArrayList<>();
        dataList = new ArrayList<>();
    }

    @Override
    public ProjectDTO toDTO()
    {
        return ProjectDTO.builder()
                .id(id)
                .name(name)
                .type(type)
                .path(path)
                .isNew(isNew)
                .isStarred(isStarred)
                .infra(infra)
                .currentVersionId(currentVersion.getId())
                .versionIdList(versionList.stream()
                        .map(Version::getId)
                        .collect(Collectors.toList()))
                .dataIdList(dataList.stream()
                        .map(Data::getId)
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public void fromDTO(ProjectDTO dto)
    {
        setName(dto.getName());
        setType(dto.getType());
        setInfra(dto.getInfra());
        update(dto);
    }

    @Override
    public void update(ProjectDTO dto)
    {
        setPath(dto.getPath());
        setIsNew(dto.getIsNew());
        setIsStarred(dto.getIsStarred());
    }

    @Override
    public List<Data> getDataList()
    {
        return new ArrayList<>(dataList);
    }

    @Override
    public List<Version> getVersionList()
    {
        return new ArrayList<>(versionList);
    }

    public void addVersion(VersionEntity version)
    {
        version.setProject(this);
        versionList.add(version);
    }

    public void addData(DataEntity data)
    {
        data.setProject(this);
        dataList.add(data);
    }

    public void setCurrentVersion(VersionEntity version)
    {
        if (! versionList.contains(version))
        {
            addVersion(version);
        }

        currentVersion = version;
    }
}
