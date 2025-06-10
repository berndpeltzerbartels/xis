package test.page.security;

import one.xis.context.IntegrationTestContext;
import one.xis.security.LocalUserInfo;
import one.xis.security.LocalUserInfoService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Disabled
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LoginAndRolesIntegrationTest {

    private IntegrationTestContext testContext;

    @BeforeAll
    void setup() {
        var userInfoService = mock(LocalUserInfoService.class);

        // Erfolgreiches Login
        var userInfo = new LocalUserInfo();
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
        loginPage.getInputElementById("login.username").setValue("admin");
        loginPage.getInputElementById("login.password").setValue("pw");
        loginPage.getElementById("login").click();

        var adminPage = testContext.openPage("/admin.html").getDocument();
        assertThat(adminPage.getElementByTagName("title").innerText).isEqualTo("Admin");
    }

    @Test
    void failedLoginReturnsToLoginPage() {
        var loginPage = testContext.openPage("/login.html").getDocument();
        loginPage.getInputElementById("login.username").setValue("admin");
        loginPage.getInputElementById("login.password").setValue("wrong");
        loginPage.getElementById("login").click();

        var doc = testContext.openPage("/login.html").getDocument();
        assertThat(doc.getElementByTagName("title").innerText).isEqualTo("Login");
    }

    @Test
    void openPageAccessibleWithoutLogin() {
        var doc = testContext.openPage("/open.html").getDocument();
        assertThat(doc.getElementByTagName("title").innerText).isEqualTo("Open");
    }

    @Test
    void methodProtectedPageBlockedWithoutLogin() {
        var doc = testContext.openPage("/mixed/method.html").getDocument();
        assertThat(doc.getElementByTagName("title").innerText).isEqualTo("Login");
    }

    @Test
    void methodProtectedPageAccessibleAfterLogin() {
        var loginPage = testContext.openPage("/login.html").getDocument();
        loginPage.getInputElementById("login.username").setValue("admin");
        loginPage.getInputElementById("login.password").setValue("pw");
        loginPage.getElementById("login").click();

        var doc = testContext.openPage("/mixed/method.html").getDocument();
        assertThat(doc.getElementByTagName("title").innerText).isEqualTo("Mixed Method");
    }
}
