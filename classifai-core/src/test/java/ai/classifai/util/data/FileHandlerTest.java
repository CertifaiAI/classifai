package ai.classifai.util.data;

import ai.classifai.data.type.image.ImageFileType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

public class FileHandlerTest {
    @ParameterizedTest
    @MethodSource("testProcessFolderSuccessSource")
    @DisplayName("Iterate folder success case")
    void testProcessFolderSuccess(File rootPath, String[] extensionFormat, int testCorrectValue) {
        List<String> outputList =  FileHandler.processFolder(rootPath, extensionFormat);
        Assertions.assertEquals(outputList.size(), testCorrectValue);
    }

    static Stream<Object> testProcessFolderSuccessSource() {
        String[] fileExtension = ImageFileType.getImageFileTypes();
        return Stream.of(
                Arguments.of(
                        new File("src/test/resources/validImages"), fileExtension, 3
                )
        );
    }

    @ParameterizedTest
    @MethodSource("testProcessFolderFailSource")
    @DisplayName("Iterate folder fail case")
    void testProcessFolderFail(File rootPath, String[] extensionFormat, int testCorrectValue) {
        List<String> outputList =  FileHandler.processFolder(rootPath, extensionFormat);
        // Value should not be equal to number of files in the input rootPath
        Assertions.assertNotEquals(outputList.size(), testCorrectValue);
    }

    static Stream<Object> testProcessFolderFailSource() {
        String[] fileExtension = ImageFileType.getImageFileTypes();
        return Stream.of(
                Arguments.of(
                        new File("src/test/resources/invalidImages"), fileExtension, 3
                )
        );
    }

    @Test
    @ValueSource(strings = {"image.jpg", "path/to/my/image.jpg"})
    @DisplayName("Check supported file")
    void testIsFileSupported(String fileName)
    {
        boolean result = FileHandler.isFileSupported(fileName, ImageFileType.getImageFileTypes());
        Assertions.assertTrue(result);
    }

}
