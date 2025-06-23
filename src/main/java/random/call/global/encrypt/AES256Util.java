package random.call.global.encrypt;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class AES256Util {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 32; // 256 bits
    private static final int IV_SIZE = 12;  // 96 bits (recommended for GCM)
    private static final int TAG_BIT_LENGTH = 128;

    // 테스트용 32바이트 키 (운영 환경에서는 환경변수에서 가져와야 함)
    private static final String SECRET_KEY = "12345678901234567890123456789012";

    private static SecretKeySpec getKeySpec() {
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != KEY_SIZE) {
            throw new IllegalArgumentException("Invalid key length: must be 32 bytes for AES-256");
        }
        return new SecretKeySpec(keyBytes, "AES");
    }

    public static String encrypt(String plainText) throws Exception {
        // 랜덤 IV 생성
        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, getKeySpec(), new GCMParameterSpec(TAG_BIT_LENGTH, iv));
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // IV + 암호문을 함께 Base64로 인코딩
        byte[] ivAndEncrypted = new byte[IV_SIZE + encrypted.length];
        System.arraycopy(iv, 0, ivAndEncrypted, 0, IV_SIZE);
        System.arraycopy(encrypted, 0, ivAndEncrypted, IV_SIZE, encrypted.length);

        return Base64.getEncoder().encodeToString(ivAndEncrypted);
    }

    public static String decrypt(String encryptedText) throws Exception {
        byte[] ivAndEncrypted = Base64.getDecoder().decode(encryptedText);

        byte[] iv = new byte[IV_SIZE];
        byte[] encrypted = new byte[ivAndEncrypted.length - IV_SIZE];
        System.arraycopy(ivAndEncrypted, 0, iv, 0, IV_SIZE);
        System.arraycopy(ivAndEncrypted, IV_SIZE, encrypted, 0, encrypted.length);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, getKeySpec(), new GCMParameterSpec(TAG_BIT_LENGTH, iv));
        byte[] decrypted = cipher.doFinal(encrypted);

        return new String(decrypted, StandardCharsets.UTF_8);
    }
}
