import ai.classifai.loader.ProjectLoader;
import ai.classifai.loader.ProjectLoaderStatus;
import ai.classifai.selector.status.FileSystemStatus;
import ai.classifai.util.collection.UuidGenerator;
import ai.classifai.util.project.ProjectInfra;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;

@Slf4j
public class LoaderSetup {


    @Getter @Setter
    private static String projectName;
    @Getter @Setter
    private static int annotationType;
    @Getter @Setter
    private static List<String> testLabelList;
    @Getter
    private static final String testDataDir = "src/test/resources/testImages";

    public static ProjectLoader createNewTestProject() {

        ProjectLoader loader = ProjectLoader.builder()
                .projectId(UuidGenerator.generateUuid())
                .projectName(projectName)
                .annotationType(annotationType)
                .projectPath(new File(testDataDir))
                .labelList(testLabelList)
                .projectLoaderStatus(ProjectLoaderStatus.LOADED)
                .projectInfra(ProjectInfra.ON_PREMISE)
                .fileSystemStatus(FileSystemStatus.ITERATING_FOLDER)
                .build();

        loader.setLabelList(testLabelList);

        return loader;
    }

}
