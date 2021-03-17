package ai.classifai.database.s3;

import lombok.Getter;

public class S3Query
{
    @Getter private static final String queue = "s3.queue";

    @Getter private static final String createS3CredentialTable = "CREATE TABLE IF NOT EXISTS S3 (cloud_id VARCHAR(50), project_id UUID, access_key VARCHAR(50), secret_access_key VARCHAR(50), bucket_list VARCHAR(200), PRIMARY KEY (cloud_id, project_id))";

    @Getter private static final String createS3Project = "INSERT INTO S3 VALUES (?, ?, ?, ?, ?)";

    @Getter private static final String retrieveS3Credential = "SELECT * FROM S3 WHERE project_id = ?";

}
