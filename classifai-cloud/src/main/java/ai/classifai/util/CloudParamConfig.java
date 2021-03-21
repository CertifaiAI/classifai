package ai.classifai.util;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/***
 * Parameters configuration for cloud-based projects
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CloudParamConfig
{
    @Getter private static String wasabiUrl = "https://s3.wasabisys.com";

    @Getter private static String wasabiTable = "Wasabi";

    @Getter private static String cloudIdParam = "cloud_id";

    @Getter private static String accessKeyParam = "access_key";

    @Getter private static String secretAccessKeyParam = "secret_access_key";

    @Getter private static String bucketParam = "bucket";
}
