package one.xis.totp;

import one.xis.auth.AdditionalLoginFactor;
import one.xis.auth.LoginFactorRegistration;
import one.xis.context.Component;

import java.util.Optional;

@Component
class TOTPLoginFactor implements AdditionalLoginFactor {

    private final TOTPStore store;
    private final TOTPSecretEncryption encryption;
    private final TOTPGenerator generator;
    private final TOTPConfig config;

    TOTPLoginFactor(TOTPStore store, TOTPSecretEncryption encryption, TOTPGenerator generator, TOTPConfig config) {
        this.store = store;
        this.encryption = encryption;
        this.generator = generator;
        this.config = config;
    }

    @Override
    public String fieldName() {
        return "totpCode";
    }

    @Override
    public boolean isRequired(String userId) {
        return userId != null && store.getEncryptedSecret(userId).isPresent();
    }

    @Override
    public boolean verify(String userId, String value) {
        return store.getEncryptedSecret(userId)
                .map(encryption::decrypt)
                .filter(secret -> generator.verify(secret, value))
                .map(secret -> acceptTimeStep(userId))
                .orElse(false);
    }

    @Override
    public Optional<LoginFactorRegistration> registration() {
        return Optional.of(new LoginFactorRegistration(config.registrationUrl(), "totp.registration.link"));
    }

    private boolean acceptTimeStep(String userId) {
        long step = generator.currentTimeStep();
        var lastAcceptedStep = store.getLastAcceptedTimeStep(userId);
        if (lastAcceptedStep.isPresent() && lastAcceptedStep.getAsLong() >= step) {
            return false;
        }
        store.saveLastAcceptedTimeStep(userId, step);
        return true;
    }
}
