package test.security;

import one.xis.auth.UserInfo;
import one.xis.context.IntegrationTestContext;
import one.xis.idp.IDPUserInfo;
import one.xis.idp.IDPUserInfoImpl;
import one.xis.idp.XisIDPConfig;
import one.xis.security.UserInfoService;
import one.xis.server.ClientRequest;
import one.xis.server.FrontendService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class IDPTest {

    static final IDPUserInfo TEST_USER = new IDPUserInfoImpl("testUser", "password", "email", Set.of("admin"), Map.of(), Set.of());


    private FrontendService frontendService;

    @BeforeEach
    void init() {
        // config to use the IDP
        var providerConfig = new XisIDPConfig();

        IntegrationTestContext testContext = IntegrationTestContext.builder()
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

    static class TestIDPUserService implements UserInfoService<UserInfo> {


        @Override
        public UserInfo getUserInfo(String userId) {
            return userId.equals(TEST_USER.getUserId()) ? TEST_USER : null;
        }

        @Override
        public void saveUserInfo(UserInfo userInfo, String idpId) {
            
        }


        @Override
        public boolean checkCredentials(String userId, String password) {
            return "password".equals(password);
        }

    }


}
