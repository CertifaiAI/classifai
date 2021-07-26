package ai.classifai.loader;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
@Builder
@Getter
public class CLIProjectImporter {

    @Builder.Default private File configFilePath = null;

}
