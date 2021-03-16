package ai.classifai.database;

import lombok.Getter;

public class S3Query
{
    @Getter private static final String queue = "s3.queue";

    @Getter private static final String createS3CredentialTable = "CREATE TABLE IF NOT EXISTS S3 (cloud_email VARCHAR(50), project_id UUID, access_key VARCHAR(50), secret_access_key VARCHAR(50), bucket VARCHAR(1000), PRIMARY KEY (cloud_email, project_id))";

    @Getter private static final String addNewCredential = "INSERT INTO S3 VALUES (?, ?, ?, ?, ?)";

    @Getter private static final String getProjectCredential = "SELECT * FROM S3 WHERE project_id = ?";

    public static void main(String[] args)
    {
        System.out.println("hello world");
    }
}
