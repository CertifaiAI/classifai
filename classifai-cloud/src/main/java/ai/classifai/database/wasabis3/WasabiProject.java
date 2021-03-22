package ai.classifai.database.wasabis3;

import ai.classifai.util.CloudParamConfig;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

import software.amazon.awssdk.services.s3.S3Client;
import lombok.NonNull;

import java.net.URI;

/**
 * Single wasabi project operation handler
 */
@Slf4j
public class WasabiProject
{


    public WasabiProject(@NonNull String accessKey, @NonNull String secretAccessKey)
    {
        AwsSessionCredentials awsCreds = AwsSessionCredentials.create(accessKey, secretAccessKey, "");

        S3Client wasabiS3Client = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .endpointOverride(URI.create(CloudParamConfig.getWasabiUrl()))
                .region(CloudParamConfig.getRegion())
                .build();


        //ObjectOps.listObjectsInBucket(wasabiS3Client, "cw-dev-testing", END_POINT);

    }
}
