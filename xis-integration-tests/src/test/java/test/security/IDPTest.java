package test.security;

import one.xis.context.IntegrationTestContext;
import one.xis.security.IDPUserService;
import one.xis.security.LocalUserInfo;
import one.xis.security.LocalUserInfoImpl;
import one.xis.security.XISIDPConfig;
import one.xis.server.ClientRequest;
import one.xis.server.FrontendService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class IDPTest {

    static final LocalUserInfo TEST_USER = new LocalUserInfoImpl("testUser", "password", Set.of("admin"), Map.of("email", "testUser@example.com"));


    private IntegrationTestContext testContext;
    private FrontendService frontendService;

    @BeforeEach
    void init() {
        // config to use the IDP
        var providerConfig = new XISIDPConfig("http://localhost:8080", "http://localhost:8080");

        testContext = IntegrationTestContext.builder()
                .withSingleton(new TestIDPUserService())
                .withSingleton(IDPTestPage.class)
                .withSingleton(providerConfig)
                .build();
        frontendService = testContext.getSingleton(FrontendService.class);
    }

    @Test
    void testIDP() {
        // we ander a protected page and expect a redirect to the IDP login page
        var clientRequest = new ClientRequest();
        clientRequest.setPageId("/idp-test.html");
        clientRequest.setClientId("abc123");
        clientRequest.setLocale(Locale.GERMAN);
        clientRequest.setZoneId("Europe/Berlin");

        var serverResponse = frontendService.processModelDataRequest(clientRequest);

        assertThat(serverResponse.getStatus()).isEqualTo(302);
        var nextUrl = serverResponse.getNextURL();

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
