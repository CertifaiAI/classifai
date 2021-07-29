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

import ai.classifai.core.entity.model.generic.Annotation;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * Abstract class for Annotation entity with hibernate implementation
 *
 * @author YinChuangSum
 */
@Getter
@Setter
@Entity(name = "annotation")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AnnotationEntity implements Annotation
{
    @Id
    @Column(name = "id")
    private Long id;

    // persist order of annotations
    @Column(name = "position")
    private Integer position;

    @ManyToOne
    @JoinColumn(name = "label_id")
    private LabelEntity label;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "data_id", referencedColumnName = "data_id"),
            @JoinColumn(name = "version_id", referencedColumnName = "version_id")
    })
    private DataVersionEntity dataVersion;

    public DataVersionEntity getDataVersion() {
        return dataVersion;
    }

    public void setDataVersion(DataVersionEntity dataVersion) {
        this.dataVersion = dataVersion;
    }
}
