package one.xis.totp;

import one.xis.UserContext;
import one.xis.auth.UserInfo;
import one.xis.auth.UserInfoImpl;
import one.xis.auth.UserInfoService;
import one.xis.context.AppContext;
import one.xis.validation.ValidatorException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TOTPAuthenticationIntegrationTest {

    private AppContext context;

    @BeforeEach
    void setUp() {
        System.setProperty("xis.totp.encryption-key", "integration-test-key");
        System.setProperty("xis.totp.issuer", "XIS Integration Test");
        context = AppContext.builder()
                .withXIS()
                .withPackage("one.xis.totp")
                .withSingletonClass(Users.class)
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

    static class Users implements UserInfoService<UserInfo> {
        @Override
        public boolean validateCredentials(String userId, String password) {
            return "alice".equals(userId) && "secret".equals(password);
        }

        @Override
        public Optional<UserInfo> getUserInfo(String userId) {
            var user = new UserInfoImpl();
            user.setUserId(userId);
            user.setRoles(Set.of("USER"));
            return Optional.of(user);
        }

        @Override
        public void saveUserInfo(UserInfo userInfo) {
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
