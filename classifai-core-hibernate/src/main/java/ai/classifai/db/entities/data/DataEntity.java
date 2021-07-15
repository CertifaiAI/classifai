package ai.classifai.db.entities.data;


import ai.classifai.core.entities.Data;
import ai.classifai.core.entities.dto.DataDTO;
import ai.classifai.db.entities.ProjectEntity;
import ai.classifai.db.entities.dataVersion.DataVersionEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.*;

@Getter
@NoArgsConstructor
@Entity(name = "DATA")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class DataEntity implements Data
{
    public static final String DATA_ID_KEY = "data_id";
    public static final String DATA_PATH_KEY = "data_path";
    public static final String CHECKSUM_KEY = "checksum";
    public static final String FILE_SIZE_KEY = "file_size";

    @Id
    @GeneratedValue
    @Column(name = DATA_ID_KEY)
    private UUID id;

    @Column(name = DATA_PATH_KEY)
    private String path;

    @Column(name = CHECKSUM_KEY)
    private String checksum;

    @Column(name = FILE_SIZE_KEY)
    private Long fileSize;

    @ManyToOne
    @JoinColumn(name= ProjectEntity.PROJECT_ID_KEY)
    private ProjectEntity project;

    @OneToMany(mappedBy = "data")
    private List<DataVersionEntity> dataVersionEntities;

    @Override
    public DataDTO toDTO()
    {
        return DataDTO.builder()
                .id(getId())
                .path(getPath())
                .fileSize(getFileSize())
                .checksum(getChecksum())
                .build();
    }
}
