package vn.qlcv.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** Password hashing based only on APIs included in the JDK. */
public final class PasswordHasher {
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private PasswordHasher() { }

    public static String hash(String password) {
        if (password == null) throw new IllegalArgumentException("Mật khẩu không được để trống.");
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        byte[] result = derive(password.toCharArray(), salt);
        // 58 characters, compatible with the existing VARCHAR(60) column.
        return "p2$" + ENCODER.encodeToString(salt) + "$" + ENCODER.encodeToString(result);
    }

    public static boolean verify(String password, String storedHash) {
        if (password == null || storedHash == null || !storedHash.startsWith("p2$")) return false;
        String[] parts = storedHash.split("\\$", -1);
        if (parts.length != 3) return false;
        try {
            byte[] salt = DECODER.decode(parts[1]);
            byte[] expected = DECODER.decode(parts[2]);
            byte[] actual = derive(password.toCharArray(), salt);
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private static byte[] derive(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception exception) {
            throw new IllegalStateException("Không thể mã hóa mật khẩu.", exception);
        } finally {
            spec.clearPassword();
        }
    }
}
