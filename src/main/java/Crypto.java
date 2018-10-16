import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.crypto.cipher.CryptoCipher;
import org.apache.commons.crypto.cipher.CryptoCipherFactory;
import org.apache.commons.crypto.cipher.CryptoCipherFactory.CipherProvider;
import org.apache.commons.crypto.utils.Utils;

public class Crypto {

    final String transform = "AES/CBC/PKCS5Padding";

    SecretKeySpec key = null;
    IvParameterSpec iv = null;
    Properties properties;

    public Crypto() {
        this.key = null;
        this.iv = null;
        this.properties = new Properties();

        Properties properties = new Properties();
        properties.setProperty(CryptoCipherFactory.CLASSES_KEY
                , CipherProvider.OPENSSL.getClassName());
    }

    public static void main(String[] args) throws Exception {

        String sampleInput = "This is a test sentence that is longer than the test before.";
        Crypto crypto = new Crypto();

        crypto.setKey(getUTF8Bytes("1234567890123456"));
        crypto.setIv(getUTF8Bytes("1234567890123456"));

        byte[] encoded = crypto.encrypt(getUTF8Bytes(sampleInput));

        String decoded = new String(crypto.decrypt(encoded), StandardCharsets.UTF_8);

        System.out.println(decoded);
    }

    public void setKey(byte[] k) {
        this.key = new SecretKeySpec(k,"AES");
    }

    public void setIv(byte[] iv) {
        this.iv = new IvParameterSpec(iv);
    }

    public byte[] encrypt(byte[] input) throws Exception {

        //Creates a CryptoCipher instance with the transformation and properties.
        CryptoCipher encipher = Utils.getCipherInstance(transform, properties);

        byte[] output = new byte[input.length + encipher.getBlockSize()];

        //Initializes the cipher with ENCRYPT_MODE, key and iv.
        encipher.init(Cipher.ENCRYPT_MODE, key, iv);
        //Continues a multiple-part encryption/decryption operation for byte array.
        int updateBytes = encipher.update(input, 0, input.length, output, 0);
        //We must call doFinal at the end of encryption/decryption.
        int finalBytes = encipher.doFinal(input, 0, 0, output, updateBytes);
        //Closes the cipher.
        encipher.close();

        return Arrays.copyOf(output, updateBytes + finalBytes);
    }

    public byte[] decrypt(byte[] encoded) throws Exception {
        // Now reverse the process using a different implementation with the same settings
        properties.setProperty(CryptoCipherFactory.CLASSES_KEY, CipherProvider.JCE.getClassName());
        CryptoCipher decipher = Utils.getCipherInstance(transform, properties);

        byte[] decoded = new byte[encoded.length];

        decipher.init(Cipher.DECRYPT_MODE, key, iv);
        decipher.doFinal(encoded, 0, encoded.length, decoded, 0);

        //String result = new String(decoded, StandardCharsets.UTF_8);

        return decoded;
    }

    /**
     * Converts String to UTF8 bytes
     *
     * @param input the input string
     * @return UTF8 bytes
     */
    public static byte[] getUTF8Bytes(String input) {
        return input.getBytes(StandardCharsets.UTF_8);
    }

}
