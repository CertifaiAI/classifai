package ai.classifai.loader;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * Project Initiator from command line argument
 *
 * @author ken479
 */

@Slf4j
@Builder
@Getter
public class CLIProjectImporter {

    @Builder.Default private File configFilePath = null;

}
