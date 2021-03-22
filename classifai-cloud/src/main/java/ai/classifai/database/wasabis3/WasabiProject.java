package ai.classifai.database.wasabis3;

import ai.classifai.util.CloudParamConfig;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

import software.amazon.awssdk.services.s3.S3Client;
import lombok.NonNull;

import java.net.URI;

/**
 * Single wasabi project operation handler
 *
 * @author codenamewei
 */
@Slf4j
@Getter
public class WasabiProject
{
    private String cloudId;
    private S3Client wasabiS3Client;
    private String wasabiBucket;

    public WasabiProject(@NonNull JsonObject objectInput)
    {
        String accessKey = objectInput.getString(CloudParamConfig.getAccessKeyParam());
        String secretAccessKey = objectInput.getString(CloudParamConfig.getSecretAccessKeyParam());

        AwsSessionCredentials awsCreds = AwsSessionCredentials.create(accessKey, secretAccessKey, "");

        //TODO how to check if client is valid
        wasabiS3Client = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .endpointOverride(URI.create(CloudParamConfig.getWasabiUrl()))
                .region(CloudParamConfig.getRegion())
                .build();

        wasabiBucket = objectInput.getString(CloudParamConfig.getBucketParam());

        cloudId = objectInput.getString(CloudParamConfig.getCloudIdParam());

    }
}
