package one.xis.totp;

import one.xis.UserContext;
import one.xis.auth.UserInfo;
import one.xis.auth.UserInfoImpl;
import one.xis.auth.UserInfoService;
import one.xis.context.AppContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TOTPSetupControllerTest {

    private AppContext context;
    private TOTPSetupController controller;

    @BeforeEach
    void setUp() {
        System.setProperty("xis.totp.encryption-key", "setup-controller-test-key");
        System.setProperty("xis.totp.issuer", "XIS Setup Controller Test");
        context = AppContext.builder()
                .withXIS()
                .withPackage("one.xis.totp")
                .withSingletonClass(Users.class)
                .withSingletonClass(Store.class)
                .build();
        controller = context.getSingleton(TOTPSetupController.class);
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("xis.totp.encryption-key");
        System.clearProperty("xis.totp.issuer");
    }

    @Test
    void freshPageModelDoesNotExposePreviousQrCode() {
        TOTPSetupResult alice = controller.setup(credentials("alice"), userContext());

        assertThat(alice.isHasQrCode()).isTrue();
        assertThat(alice.getQrCodeDataUrl()).isNotBlank();

        TOTPSetupResult freshPageModel = controller.setupResult();

        assertThat(freshPageModel.isHasQrCode()).isFalse();
        assertThat(freshPageModel.getQrCodeDataUrl()).isNull();
        assertThat(freshPageModel.getUserId()).isNull();
    }

    @Test
    void setupForDifferentUserReplacesTheQrCodeResult() {
        TOTPSetupResult alice = controller.setup(credentials("alice"), userContext());
        TOTPSetupResult bob = controller.setup(credentials("bob"), userContext());

        assertThat(bob.isHasQrCode()).isTrue();
        assertThat(bob.getUserId()).isEqualTo("bob");
        assertThat(bob.getQrCodeDataUrl()).isNotEqualTo(alice.getQrCodeDataUrl());
        assertThat(bob.getQrCodeDataUrl()).doesNotContain("alice");
    }

    @Test
    void invalidCredentialsDoNotExposeAnyQrCode() {
        TOTPSetupResult result = controller.setup(credentials("alice", "wrong"), userContext());

        assertThat(result.isHasError()).isTrue();
        assertThat(result.isHasQrCode()).isFalse();
        assertThat(result.getQrCodeDataUrl()).isNull();
        assertThat(result.getUserId()).isNull();
    }

    private TOTPSetupCredentials credentials(String username) {
        return credentials(username, "secret");
    }

    private TOTPSetupCredentials credentials(String username, String password) {
        var credentials = new TOTPSetupCredentials();
        credentials.setUsername(username);
        credentials.setPassword(password);
        return credentials;
    }

    private UserContext userContext() {
        return new UserContext() {
            @Override
            public Locale getLocale() {
                return Locale.ROOT;
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
            return Set.of("alice", "bob").contains(userId) && "secret".equals(password);
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
