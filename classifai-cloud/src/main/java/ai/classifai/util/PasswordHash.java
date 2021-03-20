package ai.classifai.util;


import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.SecretKeyFactory;
import java.security.AlgorithmParameters;
import javax.crypto.spec.IvParameterSpec;
import java.util.Base64;

@Slf4j
public class PasswordHash
{
    private final String PASS_PHASE = "ABCDEFGHIJKL";
    private final String HASH_ALGORITHM = "PBKDF2WithHmacSHA1";

    private final byte[] SALT = "12345678".getBytes();

    private final int ITERATION_COUNT = 65536;
    private int KEY_STRENGTH = 256;

    private Cipher dcipher;

    SecretKey key;
    byte[] iv;

    public PasswordHash()
    {
        try
        {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(HASH_ALGORITHM);
            KeySpec spec = new PBEKeySpec(PASS_PHASE.toCharArray(), SALT, ITERATION_COUNT, KEY_STRENGTH);
            SecretKey tmp = factory.generateSecret(spec);
            key = new SecretKeySpec(tmp.getEncoded(), "AES");
            dcipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        }
        catch(Exception e)
        {
            log.debug("Error when initiating Password Hashing function: ", e);
        }
    }

    public String encrypt(String data)
    {
        try
        {
            dcipher.init(Cipher.ENCRYPT_MODE, key);
            AlgorithmParameters params = dcipher.getParameters();
            iv = params.getParameterSpec(IvParameterSpec.class).getIV();
            byte[] utf8EncryptedData = dcipher.doFinal(data.getBytes());
            String base64EncryptedData = Base64.getEncoder().encodeToString(utf8EncryptedData);
            return base64EncryptedData;
        }
        catch(Exception e)
        {
            log.debug("Error when encrypting password: ", e);
        }

        return null;
    }

    public String decrypt(String base64EncryptedData)
    {
        try
        {
            dcipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
            byte[] decryptedData = Base64.getDecoder().decode(base64EncryptedData);
            byte[] utf8 = dcipher.doFinal(decryptedData);
            return new String(utf8, StandardCharsets.UTF_8);
        }
        catch(Exception e)
        {
            log.debug("Error when decrypting password: ", e);
        }

        return null;
    }
}