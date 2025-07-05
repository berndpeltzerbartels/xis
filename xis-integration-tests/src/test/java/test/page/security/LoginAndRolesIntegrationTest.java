package test.page.security;

import one.xis.auth.InvalidTokenException;
import one.xis.context.IntegrationTestContext;
import one.xis.idp.IDPUserInfoImpl;
import one.xis.security.UserInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class LoginAndRolesIntegrationTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void setup() throws InvalidTokenException {
        var userInfoService = mock(UserInfoService.class);

        // Erfolgreiches Login
        var userInfo = new IDPUserInfoImpl();
        userInfo.setUserId("admin");
        userInfo.setPassword("pw");
        userInfo.setRoles(Set.of("admin", "user"));
        userInfo.setClaims(Map.of());

        when(userInfoService.checkCredentials("admin", "pw")).thenReturn(true);
        when(userInfoService.getUserInfo("admin")).thenReturn(userInfo);

        // Fehlgeschlagene Authentifizierung
        when(userInfoService.checkCredentials("admin", "wrong")).thenReturn(false);

        testContext = IntegrationTestContext.builder()
                .withSingleton(userInfoService)
                .withSingleton(AdminPage.class)
                .withSingleton(MixedPage.class)
                .withSingleton(OpenPage.class)
                .build();
    }

    @Test
    void successfulLoginGrantsAccessToAdminPage() {
        var loginPage = testContext.openPage("/login.html").getDocument();
        loginPage.getInputElementById("username").setValue("admin");
        loginPage.getInputElementById("password").setValue("pw");
        loginPage.getElementByTagName("button").click();

        var adminPage = testContext.openPage("/admin.html").getDocument();
        assertThat(adminPage.getElementByTagName("title").innerText).isEqualTo("AdminPage");
    }

    @Test
    void failedLoginReturnsToLoginPage() {
        var loginPage = testContext.openPage("/login.html").getDocument();
        loginPage.getInputElementById("username").setValue("admin");
        loginPage.getInputElementById("password").setValue("wrong");
        loginPage.getElementByTagName("button").click();

        var doc = testContext.openPage("/login.html").getDocument();
        assertThat(doc.getElementByTagName("title").innerText).isEqualTo("Login");
    }

    @Test
    void openPageAccessibleWithoutLogin() {
        var doc = testContext.openPage("/open.html").getDocument();
        assertThat(doc.getElementByTagName("title").innerText).isEqualTo("OpenPage");
    }

    @Test
    void methodProtectedPageBlockedWithoutLogin() {
        var doc = testContext.openPage("/mixed.html").getDocument();
        assertThat(doc.getElementByTagName("title").innerText).isEqualTo("Login");
    }

}
