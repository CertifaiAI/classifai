package ai.classifai.core.dto;

import ai.classifai.core.utility.UuidGenerator;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TabularDTO {
    @Builder.Default String uuid = UuidGenerator.generateUuid();

    String projectId;

    String projectName;

    String filePath;

    String[] data;

    @Builder.Default String label = null;
}
