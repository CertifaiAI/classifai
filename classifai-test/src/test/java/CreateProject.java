import ai.classifai.loader.ProjectLoader;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

public class CreateProject {
    private static ProjectLoader loader = null;

    @BeforeEach
    public void prepareProject() {
        LoaderSetup.setProjectName("TestProject");
        LoaderSetup.setAnnotationType(AnnotationType.BOUNDINGBOX.ordinal());
        LoaderSetup.setTestLabelList(List.of("First", "Second", "Third"));
        loader = LoaderSetup.createNewTestProject();
    }

    @Test
    @DisplayName("Create project")
    void testCreateProject() {
        Assertions.assertEquals(
                List.of(loader.getProjectName(), loader.getAnnotationType(), loader.getLabelList()),
                List.of(LoaderSetup.getProjectName(), LoaderSetup.getAnnotationType(), LoaderSetup.getTestLabelList())
        );
    }

    @Test
    @DisplayName("Create project server up")
    void testCreateProjectServerUp(Vertx vertx, Vertx testContext) {

    }

}
