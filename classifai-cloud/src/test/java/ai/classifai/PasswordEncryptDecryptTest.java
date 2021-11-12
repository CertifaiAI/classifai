package ai.classifai;

import ai.classifai.core.util.PasswordHash;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Password hashing test
 *
 * @author codenamewei
 */
@Slf4j
public class PasswordEncryptDecryptTest
{
    private static PasswordHash passwordHash;

    @BeforeAll
    public static void setup()
    {
        passwordHash = new PasswordHash();
    }

    @Test
    public void passwordTestEqual()
    {
        String testInput = "the quick brown fox jumps over the lazy dog";

        String encrypted = passwordHash.encrypt(testInput);

        String decrypted = passwordHash.decrypt(encrypted);

        //input should be same with decryption of input
        Assertions.assertEquals(testInput, decrypted);
    }

    @Test
    public void passwordTestNotEqual()
    {
        String testInput = "testInput";
        String anotherTestInput = "testinput";

        String encrypted = passwordHash.encrypt(testInput);

        String decrypted = passwordHash.decrypt(encrypted);

        //input should be not same with another input which slightly varies
        Assertions.assertNotEquals(anotherTestInput, decrypted);
    }


}
