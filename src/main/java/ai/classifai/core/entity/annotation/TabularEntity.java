package ai.classifai.core.entity.annotation;

import ai.classifai.core.dto.TabularDTO;

public class TabularEntity implements AnnotationEntity<TabularDTO> {
    String projectId;

    String filePath;

    Long fileSize;

    @Override
    public TabularDTO toDto() {
        return null;
    }
}
