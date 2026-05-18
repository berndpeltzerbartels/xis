package one.xis.totp;

import one.xis.context.AppContextInitializedEvent;
import one.xis.context.Component;
import one.xis.context.EventListener;

@Component
class TOTPStartupValidator {

    private final TOTPConfig config;

    TOTPStartupValidator(TOTPConfig config) {
        this.config = config;
    }

    @EventListener
    void validate(AppContextInitializedEvent event) {
        int storeCount = event.getAppContext().getSingletons(TOTPStore.class).size();
        if (storeCount == 0) {
            throw new IllegalStateException("xis-totp requires exactly one TOTPStore implementation");
        }
        if (storeCount > 1) {
            throw new IllegalStateException("xis-totp requires exactly one TOTPStore implementation, but multiple implementations were found");
        }
        if (config.encryptionKey() == null || config.encryptionKey().isBlank()) {
            throw new IllegalStateException("xis.totp.encryption-key must be configured when xis-totp is used");
        }
    }
}
