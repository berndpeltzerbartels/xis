package one.xis.totp;

import one.xis.UserContext;
import one.xis.auth.LocalCredentialService;
import one.xis.auth.CodeStore;
import one.xis.context.AppContext;
import one.xis.context.IntegrationTestContext;
import one.xis.context.TestClient;
import one.xis.validation.ValidatorException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

class TOTPAuthenticationIntegrationTest {

    private AppContext context;

    @BeforeEach
    void setUp() {
        System.setProperty("xis.totp.encryption-key", "integration-test-key");
        System.setProperty("xis.totp.issuer", "XIS Integration Test");
        context = AppContext.builder()
                .withXIS()
                .withPackage("one.xis.totp")
                .withSingletonClass(Credentials.class)
                .withSingletonClass(Store.class)
                .build();
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("xis.totp.encryption-key");
        System.clearProperty("xis.totp.issuer");
    }

    @Test
    void acceptsLoginWithValidTotpCode() {
        String code = currentCode(provisioningUri("alice"));

        assertThatCode(() -> validate("alice", "secret", code))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsLoginWithInvalidTotpCode() {
        provisioningUri("alice");

        assertThatThrownBy(() -> validate("alice", "secret", "000000"))
                .isInstanceOf(ValidatorException.class);
    }

    @Test
    void loginFormRedirectsToLocalCallbackWithValidPasswordAndTotpCode() {
        var testContext = loginTestContext();
        var client = testContext.openPage("/login.html?redirect_uri=/after-login.html");
        String code = currentCode(testContext.getSingleton(TOTPProvisioningService.class).provisioningUri("alice"));

        submitLogin(client, "alice", "secret", code);

        assertThat(client.getWindow().location.href)
                .startsWith("/xis/auth/callback/local?state=")
                .contains("&code=");
        assertThat(testContext.getSingleton(CodeStore.class).getUserIdForCode(callbackCode(client)))
                .isEqualTo("alice");
    }

    @Test
    void loginFormRejectsWrongPasswordBeforeTotp() {
        var testContext = loginTestContext();
        var client = testContext.openPage("/login.html?redirect_uri=/after-login.html");
        String code = currentCode(testContext.getSingleton(TOTPProvisioningService.class).provisioningUri("alice"));

        submitLogin(client, "alice", "wrong", code);

        assertThat(client.getWindow().location.href).isEqualTo("http://testserver/login.html?redirect_uri=/after-login.html");
    }

    @Test
    void loginFormRejectsWrongTotpCode() {
        var testContext = loginTestContext();
        var client = testContext.openPage("/login.html?redirect_uri=/after-login.html");
        testContext.getSingleton(TOTPProvisioningService.class).provisioningUri("alice");

        submitLogin(client, "alice", "secret", "000000");

        assertThat(client.getWindow().location.href).isEqualTo("http://testserver/login.html?redirect_uri=/after-login.html");
    }

    private void validate(String username, String password, String totpCode) throws ValidatorException {
        try {
            Class<?> loginDataType = Class.forName("one.xis.auth.LoginData");
            Constructor<?> constructor = loginDataType.getDeclaredConstructor(String.class, String.class, String.class, String.class);
            constructor.setAccessible(true);
            Object loginData = constructor.newInstance(username, password, totpCode, "state");

            Class<?> validatorType = Class.forName("one.xis.auth.LoginValidator");
            Object validator = context.getSingleton(validatorType);
            Method validate = validatorType.getDeclaredMethod("validate", loginDataType, java.lang.reflect.AnnotatedElement.class, UserContext.class);
            validate.setAccessible(true);
            validate.invoke(validator, loginData, getClass(), userContext());
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getTargetException();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            if (cause instanceof ValidatorException validatorException) {
                throw validatorException;
            }
            throw new IllegalStateException("Unexpected login validation failure", cause);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to validate login", e);
        }
    }

    private String provisioningUri(String userId) {
        return context.getSingleton(TOTPProvisioningService.class).provisioningUri(userId);
    }

    private IntegrationTestContext loginTestContext() {
        System.setProperty("xis.totp.encryption-key", "login-flow-test-key");
        System.setProperty("xis.totp.issuer", "XIS Login Flow Test");
        return IntegrationTestContext.builder()
                .withPackage("one.xis.auth")
                .withPackage("one.xis.totp")
                .withSingleton(Credentials.class)
                .withSingleton(Store.class)
                .build();
    }

    private void submitLogin(TestClient client, String username, String password, String totpCode) {
        var document = client.getDocument();
        document.getInputElementById("username").setValue(username);
        document.getInputElementById("password").setValue(password);
        document.getInputElementById("totpCode").setValue(totpCode);
        document.getElementByTagName("button").click();
    }

    private String callbackCode(TestClient client) {
        String href = client.getWindow().location.href;
        int queryStart = href.indexOf('?');
        assertThat(queryStart).isGreaterThanOrEqualTo(0);
        for (String parameter : href.substring(queryStart + 1).split("&")) {
            String[] pair = parameter.split("=", 2);
            if ("code".equals(pair[0])) {
                return URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
            }
        }
        throw new AssertionError("No code parameter in " + href);
    }

    private String currentCode(String provisioningUri) {
        return code(secret(provisioningUri), System.currentTimeMillis() / 1000 / 30);
    }

    private String secret(String provisioningUri) {
        for (String parameter : provisioningUri.substring(provisioningUri.indexOf('?') + 1).split("&")) {
            String[] pair = parameter.split("=", 2);
            if ("secret".equals(pair[0])) {
                return pair[1];
            }
        }
        throw new IllegalArgumentException("No secret in provisioning URI");
    }

    private String code(String base32Secret, long timeStep) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(Base32.decode(base32Secret), "HmacSHA1"));
            byte[] hash = mac.doFinal(ByteBuffer.allocate(Long.BYTES).putLong(timeStep).array());
            int offset = hash[hash.length - 1] & 0x0f;
            int binary = ((hash[offset] & 0x7f) << 24)
                    | ((hash[offset + 1] & 0xff) << 16)
                    | ((hash[offset + 2] & 0xff) << 8)
                    | (hash[offset + 3] & 0xff);
            return String.format("%06d", binary % 1_000_000);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create TOTP code", e);
        }
    }

    private UserContext userContext() {
        return new UserContext() {
            @Override
            public java.util.Locale getLocale() {
                return java.util.Locale.ROOT;
            }

            @Override
            public java.time.ZoneId getZoneId() {
                return java.time.ZoneOffset.UTC;
            }

            @Override
            public String getClientId() {
                return "client";
            }

            @Override
            public String getUserId() {
                return null;
            }

            @Override
            public Set<String> getRoles() {
                return Set.of();
            }

            @Override
            public boolean isAuthenticated() {
                return false;
            }
        };
    }

    static class Credentials implements LocalCredentialService {
        @Override
        public boolean validateCredentials(String userId, String password) {
            return "alice".equals(userId) && "secret".equals(password);
        }

        @Override
        public void setPassword(String userId, String password) {
        }

        @Override
        public boolean needsRehash(String userId) {
            return false;
        }
    }

    static class Store implements TOTPStore {
        private final Map<String, String> secrets = new HashMap<>();
        private final Map<String, Long> acceptedSteps = new HashMap<>();

        @Override
        public Optional<String> getEncryptedSecret(String userId) {
            return Optional.ofNullable(secrets.get(userId));
        }

        @Override
        public void saveEncryptedSecret(String userId, String encryptedSecret) {
            secrets.put(userId, encryptedSecret);
        }

        @Override
        public OptionalLong getLastAcceptedTimeStep(String userId) {
            Long step = acceptedSteps.get(userId);
            return step == null ? OptionalLong.empty() : OptionalLong.of(step);
        }

        @Override
        public void saveLastAcceptedTimeStep(String userId, long timeStep) {
            acceptedSteps.put(userId, timeStep);
        }
    }
}
