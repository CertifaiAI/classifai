package ai.classifai.database.wasabis3;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Table to store Wasabi S3 credentials
 *
 * @author codenamewei
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WasabiQuery
{
    @Getter private static final String queue = "wasabi.queue";

    @Getter private static final String createTable = "CREATE TABLE IF NOT EXISTS Wasabi (cloud_id VARCHAR(50), project_id UUID, access_key VARCHAR(200), secret_access_key VARCHAR(200), bucket VARCHAR(50), PRIMARY KEY (cloud_id, project_id))";

    @Getter private static final String writeCredential = "INSERT INTO Wasabi VALUES (?, ?, ?, ?, ?)";

    @Getter private static final String retrieveCredential = "SELECT * FROM Wasabi WHERE project_id = ?";
}
