package one.xis.totp;

import one.xis.context.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
class TOTPProvisioningService {

    private final TOTPStore store;
    private final TOTPSecretEncryption encryption;
    private final TOTPGenerator generator;
    private final TOTPConfig config;

    TOTPProvisioningService(TOTPStore store, TOTPSecretEncryption encryption, TOTPGenerator generator, TOTPConfig config) {
        this.store = store;
        this.encryption = encryption;
        this.generator = generator;
        this.config = config;
    }

    String getOrCreateSecret(String userId) {
        return store.getEncryptedSecret(userId)
                .map(encryption::decrypt)
                .orElseGet(() -> createAndStoreSecret(userId));
    }

    String provisioningUri(String userId) {
        String issuer = config.issuer();
        String label = urlEncode(issuer + ":" + userId);
        return "otpauth://totp/" + label
                + "?secret=" + urlEncode(getOrCreateSecret(userId))
                + "&issuer=" + urlEncode(issuer)
                + "&algorithm=SHA1&digits=6&period=" + TOTPGenerator.PERIOD_SECONDS;
    }

    private String createAndStoreSecret(String userId) {
        String secret = generator.createSecret();
        store.saveEncryptedSecret(userId, encryption.encrypt(secret));
        return secret;
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
