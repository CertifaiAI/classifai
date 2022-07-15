package ai.classifai.core.entity.annotation;

import ai.classifai.core.dto.TabularDTO;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TabularEntity implements AnnotationEntity<TabularDTO> {
    String uuid;

    String projectId;

    String projectName;

    String filePath;

    String[] data;

    String label;

    @Override
    public TabularDTO toDto() {
        return TabularDTO.builder()
                .uuid(uuid)
                .projectId(projectId)
                .projectName(projectName)
                .filePath(filePath)
                .data(data)
                .label(label)
                .build();
    }
}
