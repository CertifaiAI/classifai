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

import ai.classifai.core.entity.model.generic.Data;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Abstract class for Data entity with hibernate implementation
 *
 * @author YinChuangSum
 */
@Slf4j
@lombok.Data
@Entity(name = "data")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class DataEntity implements Data
{
    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @Column(name = "path")
    private String path;

    @Column(name = "checksum")
    private String checksum;

    @Column(name = "file_size")
    private Long fileSize;

    @ManyToOne
    @JoinColumn(name="project_id")
    private ProjectEntity project;

    @OneToMany(mappedBy = "data",
            cascade = CascadeType.ALL)
    private List<DataVersionEntity> dataVersionList;

    protected DataEntity()
    {
        dataVersionList = new ArrayList<>();
    }

    public void addDataVersion(DataVersionEntity dataVersion)
    {
        dataVersion.setData(this);
        dataVersionList.add(dataVersion);
    }
}
