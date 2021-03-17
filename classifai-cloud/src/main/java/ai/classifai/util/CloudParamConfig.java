package ai.classifai.util;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CloudParamConfig
{
    @Getter private static String s3Table = "S3";

    @Getter private static String cloudIdParam = "cloud_id";

    @Getter private static String accessKeyParam = "access_key";

    @Getter private static String secretAccessKeyParam = "secret_access_key";

    @Getter private static String bucketParam = "bucket";

    @Getter private static String bucketListParam = "bucket_list";

    @Getter private static String s3Url = "https://s3.wasabisys.com";

}
