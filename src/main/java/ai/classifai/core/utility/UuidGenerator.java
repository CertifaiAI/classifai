package ai.classifai.core.utility;

import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * UUID Generator
 *
 * @author YCCertifai
 */
@NoArgsConstructor
public class UuidGenerator
{
    public static String generateUuid()
    {
        String newID = UUID.randomUUID().toString();

        return newID;
    }
}