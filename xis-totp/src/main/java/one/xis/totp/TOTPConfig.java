package one.xis.totp;

import one.xis.context.ApplicationProperties;
import one.xis.context.Component;

@Component
class TOTPConfig {

    String issuer() {
        return property("xis.totp.issuer", "XIS");
    }

    String encryptionKey() {
        return property("xis.totp.encryption-key", null);
    }

    String registrationUrl() {
        return property("xis.totp.registration-url", "/totp-setup.html");
    }

    private String property(String key, String defaultValue) {
        String systemProperty = System.getProperty(key);
        if (systemProperty != null) {
            return systemProperty;
        }
        return ApplicationProperties.getProperty(key, defaultValue);
    }
}
