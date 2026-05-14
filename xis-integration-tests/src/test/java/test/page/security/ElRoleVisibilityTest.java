package test.page.security;

import one.xis.auth.UserInfoImpl;
import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ElRoleVisibilityTest {

    @Test
    @DisplayName("EL role functions can show frontend-only controls for matching roles")
    void elRoleFunctionsShowMatchingControls() {
        var userInfo = new UserInfoImpl();
        userInfo.setUserId("admin-user");
        userInfo.setRoles(Set.of("ADMIN"));

        var testContext = IntegrationTestContext.builder()
                .withSingleton(ElRoleVisibilityPage.class)
                .withLoggedInUser(userInfo, "passwd")
                .build();

        var document = testContext.openPage("/el-role-visibility.html").getDocument();

        assertThat(document.getElementById("admin-action")).isNotNull();
        assertThat(document.getElementById("support-or-admin-action")).isNotNull();
        assertThat(document.getElementById("array-role-action")).isNotNull();
        assertThat(document.getElementById("user-action")).isNull();
    }

    @Test
    @DisplayName("EL role functions are false for anonymous users")
    void elRoleFunctionsAreFalseForAnonymousUsers() {
        var testContext = IntegrationTestContext.builder()
                .withSingleton(ElRoleVisibilityPage.class)
                .build();

        var document = testContext.openPage("/el-role-visibility.html").getDocument();

        assertThat(document.getElementById("admin-action")).isNull();
        assertThat(document.getElementById("support-or-admin-action")).isNull();
        assertThat(document.getElementById("array-role-action")).isNull();
        assertThat(document.getElementById("user-action")).isNull();
    }
}
