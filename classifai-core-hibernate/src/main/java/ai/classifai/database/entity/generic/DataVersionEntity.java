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
import ai.classifai.core.entity.model.generic.DataVersion;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Abstract class for DataVersion entity with hibernate implementation
 *
 * @author YinChuangSum
 */
@lombok.Data
@Entity(name = "data_version")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class DataVersionEntity implements DataVersion
{
    @EmbeddedId
    private DataVersionKey id;

    @ManyToOne
    @MapsId(value = "dataId")
    @JoinColumn(name = "data_id")
    private DataEntity data;

    @ManyToOne
    @MapsId(value = "versionId")
    @JoinColumn(name = "version_id")
    private VersionEntity version;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "dataVersion")
    private List<AnnotationEntity> annotationList;

    public DataVersionEntity()
    {
        annotationList = new ArrayList<>();
    }

    public void addAnnotation(AnnotationEntity annotation)
    {
        annotation.setDataVersion(this);
        annotationList.add(annotation);
    }

    public void removeAnnotation(AnnotationEntity annotation)
    {
        annotationList.remove(annotation);
    }

    @Override
    public List<Annotation> getAnnotations()
    {
        return new ArrayList<>(annotationList);
    }
}
