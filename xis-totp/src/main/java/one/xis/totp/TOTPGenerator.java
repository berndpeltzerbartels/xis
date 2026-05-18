package one.xis.totp;

import one.xis.context.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.Clock;

@Component
class TOTPGenerator {

    static final int PERIOD_SECONDS = 30;
    private static final int DIGITS = 6;

    private final SecureRandom secureRandom = new SecureRandom();
    private Clock clock = Clock.systemUTC();

    String createSecret() {
        byte[] bytes = new byte[20];
        secureRandom.nextBytes(bytes);
        return Base32.encode(bytes);
    }

    boolean verify(String secret, String code) {
        if (code == null || !code.matches("\\d{6}")) {
            return false;
        }
        long step = currentTimeStep();
        for (long candidate = step - 1; candidate <= step + 1; candidate++) {
            if (constantTimeEquals(code, code(secret, candidate))) {
                return true;
            }
        }
        return false;
    }

    long currentTimeStep() {
        return clock.instant().getEpochSecond() / PERIOD_SECONDS;
    }

    String code(String base32Secret, long timeStep) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(Base32.decode(base32Secret), "HmacSHA1"));
            byte[] hash = mac.doFinal(ByteBuffer.allocate(Long.BYTES).putLong(timeStep).array());
            int offset = hash[hash.length - 1] & 0x0f;
            int binary = ((hash[offset] & 0x7f) << 24)
                    | ((hash[offset + 1] & 0xff) << 16)
                    | ((hash[offset + 2] & 0xff) << 8)
                    | (hash[offset + 3] & 0xff);
            int otp = binary % (int) Math.pow(10, DIGITS);
            return String.format("%0" + DIGITS + "d", otp);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Unable to create TOTP code", e);
        }
    }

    void setClock(Clock clock) {
        this.clock = clock;
    }

    private boolean constantTimeEquals(String left, String right) {
        if (left.length() != right.length()) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < left.length(); i++) {
            diff |= left.charAt(i) ^ right.charAt(i);
        }
        return diff == 0;
    }
}
