package ai.classifai.util;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.regions.Region;

/***
 * Parameters configuration for cloud-based projects
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CloudParamConfig
{
    @Getter private static final String wasabiUrl = "https://s3.wasabisys.com";

    @Getter private static final String wasabiTable = "Wasabi";

    @Getter private static final String cloudIdParam = "cloud_id";

    @Getter private static final String accessKeyParam = "access_key";

    @Getter private static final String secretAccessKeyParam = "secret_access_key";

    @Getter private static final String bucketParam = "bucket";

    //FIXME: hardcode of region
    @Getter private static final Region region = Region.US_EAST_1;
}
