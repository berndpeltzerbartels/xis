package one.xis.auth;

import one.xis.context.AppContext;
import one.xis.http.RequestContext;
import one.xis.server.ClientRequest;
import one.xis.server.FrontendService;
import one.xis.server.ServerResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class LocalAuthenticationIntegrationTest {


    @Nested
    @DisplayName("Request without Token to Page without @Roles")
    class NoTokenNoRoles {

        private AppContext context;

        @BeforeEach
        void init() {
            context = AppContext.builder()
                    .withSingletonClass(TestUserInfoService.class)
                    .withXIS().build();
        }

        private ClientRequest clientRequest;

        @BeforeEach
        void setUp() {
            clientRequest = new ClientRequest();
            clientRequest.setPageId("/test.html");
            clientRequest.setClientId("test-client");
            clientRequest.setLocale(Locale.GERMANY);
            clientRequest.setZoneId("Europe/Berlin");
            RequestContext.createInstance(mock(), mock());
        }

        @Test
        void processModelDataRequest_shouldSucceed() {
            FrontendService frontendService = context.getSingleton(FrontendService.class);

            ServerResponse response = assertDoesNotThrow(() -> frontendService.processModelDataRequest(clientRequest));

            assertThat(response).isNotNull();
            assertThat(response.getData()).containsEntry("message", "Success");
        }
    }

    @Nested
    @DisplayName("Request with Invalid Token to Page with @Roles")
    class InvalidToken {

        private AppContext context;

        @BeforeEach
        void init() {
            context = AppContext.builder()
                    .withSingletonClass(TestUserInfoService.class)
                    .withXIS().build();
        }

        private ClientRequest clientRequest;

        @BeforeEach
        void setUp() {
            clientRequest = new ClientRequest();
            clientRequest.setPageId("/protected.html");
            clientRequest.setClientId("test-client");
            clientRequest.setLocale(Locale.GERMANY);
            clientRequest.setZoneId("Europe/Berlin");
            clientRequest.setAccessToken("this-is-an-invalid-token");
            RequestContext.createInstance(mock(), mock());
        }

        @Test
        void processModelDataRequest_shouldReturnForbidden() {
            FrontendService frontendService = context.getSingleton(FrontendService.class);
            
            assertThrows(AuthenticationException.class, () -> frontendService.processModelDataRequest(clientRequest));
        }
    }


    static class TestUserInfoService implements UserInfoService<UserInfo> {

        private final Collection<UserInfo> userInfos = new HashSet<>();
        private final Map<String, String> userPasswords = new HashMap<>();

        @Override
        public boolean validateCredentials(String userId, String password) {
            return userPasswords.getOrDefault(userId, "").equals(password);
        }

        @Override
        public Optional<UserInfo> getUserInfo(String userId) {
            return userInfos.stream()
                    .filter(userInfo -> userInfo.getUserId().equals(userId))
                    .findFirst();
        }

        public void saveUserInfo(UserInfo userInfo, String password) {
            saveUserInfo(userInfo);
            userPasswords.put(userInfo.getUserId(), password);
        }

        public void saveUserInfo(UserInfo userInfo) {
            getUserInfo(userInfo.getUserId()).map(UserInfoImpl.class::cast).ifPresentOrElse(
                    existingUserInfo -> existingUserInfo.setRoles(userInfo.getRoles()),
                    () -> userInfos.add(userInfo)
            );
        }
    }
}