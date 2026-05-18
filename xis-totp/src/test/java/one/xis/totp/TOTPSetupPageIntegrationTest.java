package one.xis.totp;

import one.xis.auth.UserInfo;
import one.xis.auth.UserInfoImpl;
import one.xis.auth.UserInfoService;
import one.xis.context.IntegrationTestContext;
import one.xis.test.dom.Document;
import one.xis.test.dom.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TOTPSetupPageIntegrationTest {

    private IntegrationTestContext context;

    @BeforeEach
    void setUp() {
        System.setProperty("xis.totp.encryption-key", "setup-page-integration-test-key");
        System.setProperty("xis.totp.issuer", "XIS Setup Page Integration Test");
        context = IntegrationTestContext.builder()
                .withPackage("one.xis.totp")
                .withSingleton(Users.class)
                .withSingleton(Store.class)
                .build();
    }

    @Test
    void setupActionPublishesQrCodeOnlyAsCurrentModelDelta() {
        var document = context.openPage("/totp-setup.html").getDocument();
        assertThat(qrCodes(document)).isZero();

        submitSetup(document, "totpSetupAlice");

        assertThat(qrCodes(document)).isEqualTo(1);
        assertThat(document.getBody().getInnerText()).contains("totpSetupAlice");

        var freshDocument = context.openPage("/totp-setup.html").getDocument();
        assertThat(qrCodes(freshDocument)).isZero();
        assertThat(freshDocument.getBody().getInnerText()).doesNotContain("totpSetupAlice");

        submitSetup(freshDocument, "totpSetupBob");

        assertThat(qrCodes(freshDocument)).isEqualTo(1);
        assertThat(freshDocument.getBody().getInnerText()).contains("totpSetupBob");
        assertThat(freshDocument.getBody().getInnerText()).doesNotContain("totpSetupAlice");
    }

    private void submitSetup(Document document, String username) {
        document.getInputElementById("totp-setup-username").setValue(username);
        document.getInputElementById("totp-setup-password").setValue("secret");
        document.getElementById("totp-setup-button").click();
    }

    private int qrCodes(Document document) {
        return (int) document.getElementsByTagName("img").stream()
                .filter(element -> {
                    String src = ((Element) element).getAttribute("src");
                    return src != null && src.startsWith("data:image/svg+xml");
                })
                .count();
    }

    static class Users implements UserInfoService<UserInfo> {
        @Override
        public boolean validateCredentials(String userId, String password) {
            return Set.of("totpSetupAlice", "totpSetupBob").contains(userId) && "secret".equals(password);
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
