package test.security;

import one.xis.context.IntegrationTestContext;
import one.xis.security.IDPUserService;
import one.xis.security.LocalUserInfo;
import one.xis.security.StateParameter;
import one.xis.security.XISIDPProviderConfig;
import one.xis.server.FrontendService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

class IDPTest {

    static final LocalUserInfo TEST_USER = new LocalUserInfo("testUser", "password", Set.of("admin"), Map.of("email", "testUser@example.com"));


    private IntegrationTestContext testContext;
    private FrontendService frontendService;

    @BeforeEach
    void init() {
        // config to use the IDP
        var providerConfig = new XISIDPProviderConfig("http://localhost:8080", "http://localhost:8080");

        testContext = IntegrationTestContext.builder()
                .withSingleton(new TestIDPUserService())
                .withSingleton(IDPTestPage.class)
                .withSingleton(providerConfig)
                .build();
        frontendService = testContext.getSingleton(FrontendService.class);
    }

    @Test
    void redirectIfLackOfPrivileges() {
        var state = StateParameter.create("/xyz.html");
        // TODO
    }

    static class TestIDPUserService implements IDPUserService {


        @Override
        public LocalUserInfo getUserInfo(String userId) {
            return userId.equals(TEST_USER.getUserId()) ? TEST_USER : null;
        }

        @Override
        public Collection<String> getAllowedRedirectUrls() {
            return List.of("/xyz");
        }

        @Override
        public boolean checkCredentials(String userId, String password) {
            return "password".equals(password);
        }

    }


}
