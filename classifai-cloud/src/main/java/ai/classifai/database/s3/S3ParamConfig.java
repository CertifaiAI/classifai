package ai.classifai.database.s3;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class S3ParamConfig
{
    @Getter private static String s3Table = "S3";

    @Getter private static String cloudEmailParam = "cloud_email";

    @Getter private static String accessKeyParam = "access_key";

    @Getter private static String secretAccessKeyParam = "secret_access_key";

    @Getter private static String bucketParam = "bucket";
}
