package test.page.security;

import one.xis.auth.UserInfoImpl;
import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Hierarchical Roles Tests")
public class HierarchicalRolesTest {


    @Nested
    @DisplayName("Controller Level - Single Role Required")
    class ControllerLevelTest {

        @Test
        @DisplayName("Access granted with USER role")
        void testAccessWithUserRole() {
            var userInfo = new UserInfoImpl();
            userInfo.setUserId("user1");
            userInfo.setRoles(Set.of("USER"));

            var testContext = IntegrationTestContext.builder()
                    .withSingleton(HierarchicalRolesTestPage.class)
                    .withLoggedInUser(userInfo, "passwd")
                    .build();

            var result = testContext.openPage("/hierarchical-roles.html");
            assertThat(result.getDocument().getElementById("pageData").getInnerText())
                    .isEqualTo("Page accessible");
        }

        @Test
        @DisplayName("Access granted with VERIFIED role (alternative)")
        void testAccessWithVerifiedRole() {
            var userInfo = new UserInfoImpl();
            userInfo.setUserId("user1");
            userInfo.setRoles(Set.of("VERIFIED"));

            var testContext = IntegrationTestContext.builder()
                    .withSingleton(HierarchicalRolesTestPage.class)
                    .withLoggedInUser(userInfo, "passwd")
                    .build();

            var result = testContext.openPage("/hierarchical-roles.html");
            assertThat(result.getDocument().getElementById("pageData").getInnerText())
                    .isEqualTo("Page accessible");
        }

        @Test
        @DisplayName("Access denied without required controller roles")
        void testAccessDeniedWithoutControllerRoles() {
            var userInfo = new UserInfoImpl();
            userInfo.setUserId("user1");
            userInfo.setRoles(Set.of("ADMIN"));

            var testContext = IntegrationTestContext.builder()
                    .withSingleton(HierarchicalRolesTestPage.class)
                    .withLoggedInUser(userInfo, "passwd")
                    .build();

            // User with only ADMIN role cannot access page requiring USER or VERIFIED
            // Should redirect to login page
            var result = testContext.openPage("/hierarchical-roles.html");
            assertThat(result.getWindow().location.href).contains("/login.html");
        }
    }

    @Nested
    @DisplayName("Method Level - Additional Role Required")
    class MethodLevelTest {

        @Test
        @DisplayName("Action requires only controller role")
        void testActionWithControllerRoleOnly() {
            var userInfo = new UserInfoImpl();
            userInfo.setUserId("user1");
            userInfo.setRoles(Set.of("USER"));

            var testContext = IntegrationTestContext.builder()
                    .withSingleton(HierarchicalRolesTestPage.class)
                    .withLoggedInUser(userInfo, "passwd")
                    .build();

            var result = testContext.openPage("/hierarchical-roles.html");
            var button = result.getDocument().getElementById("actionWithControllerRoleOnly");

            // Should not throw
            button.click();
        }

        @Test
        @DisplayName("Action requires controller AND method role")
        void testActionWithBothRoles() {
            var userInfo = new UserInfoImpl();
            userInfo.setUserId("user1");
            userInfo.setRoles(Set.of("USER", "ADMIN"));

            var testContext = IntegrationTestContext.builder()
                    .withSingleton(HierarchicalRolesTestPage.class)
                    .withLoggedInUser(userInfo, "passwd")
                    .build();

            var result = testContext.openPage("/hierarchical-roles.html");
            var button = result.getDocument().getElementById("actionWithControllerAndMethodRoles");

            // Should not throw
            button.click();
        }

        @Test
        @DisplayName("Action denied with only controller role")
        void testActionDeniedWithOnlyControllerRole() {
            var userInfo = new UserInfoImpl();
            userInfo.setUserId("user1");
            userInfo.setRoles(Set.of("USER"));

            var testContext = IntegrationTestContext.builder()
                    .withSingleton(HierarchicalRolesTestPage.class)
                    .withLoggedInUser(userInfo, "passwd")
                    .build();

            var result = testContext.openPage("/hierarchical-roles.html");
            var button = result.getDocument().getElementById("actionWithControllerAndMethodRoles");

            // Should redirect to login page when method role is missing
            button.click();
            assertThat(result.getWindow().location.href).contains("/login.html");
        }

        @Test
        @DisplayName("Action with alternative method role (MODERATOR)")
        void testActionWithAlternativeMethodRole() {
            var userInfo = new UserInfoImpl();
            userInfo.setUserId("user1");
            userInfo.setRoles(Set.of("VERIFIED", "MODERATOR"));

            var testContext = IntegrationTestContext.builder()
                    .withSingleton(HierarchicalRolesTestPage.class)
                    .withLoggedInUser(userInfo, "passwd")
                    .build();

            var result = testContext.openPage("/hierarchical-roles.html");
            var button = result.getDocument().getElementById("actionWithControllerAndMethodRoles");

            // Should not throw (VERIFIED satisfies controller, MODERATOR satisfies method)
            button.click();
        }
        
        @Test
        @DisplayName("Method-only role requirement without controller roles")
        void testMethodRoleOnlyWithoutControllerRoles() {
            var userInfo = new UserInfoImpl();
            userInfo.setUserId("user1");
            userInfo.setRoles(Set.of("ADMIN"));

            var testContext = IntegrationTestContext.builder()
                    .withSingleton(NoControllerRolesTestPage.class)
                    .withLoggedInUser(userInfo, "passwd")
                    .build();

            var result = testContext.openPage("/no-controller-roles.html");
            var button = result.getDocument().getElementById("actionWithMethodRolesOnly");

            // Should not throw - only method role required
            button.click();
        }
    }

    @Nested
    @DisplayName("Parameter Level - DTO Role Required")
    class ParameterLevelTest {

        @Test
        @DisplayName("Action with DTO requires all three levels")
        void testActionWithDtoRequiresAllLevels() {
            var userInfo = new UserInfoImpl();
            userInfo.setUserId("user1");
            userInfo.setRoles(Set.of("USER", "SUPPORT", "DATA_EDITOR"));

            var testContext = IntegrationTestContext.builder()
                    .withSingleton(HierarchicalRolesTestPage.class)
                    .withLoggedInUser(userInfo, "passwd")
                    .build();

            var result = testContext.openPage("/hierarchical-roles.html");
            var button = result.getDocument().getElementById("actionWithAllThreeLevels");

            // Should not throw
            button.click();
        }

        @Test
        @DisplayName("Action with DTO denied without DTO role")
        void testActionWithDtoDeniedWithoutDtoRole() {
            var userInfo = new UserInfoImpl();
            userInfo.setUserId("user1");
            userInfo.setRoles(Set.of("USER", "SUPPORT"));

            var testContext = IntegrationTestContext.builder()
                    .withSingleton(HierarchicalRolesTestPage.class)
                    .withLoggedInUser(userInfo, "passwd")
                    .build();

            var result = testContext.openPage("/hierarchical-roles.html");
            var button = result.getDocument().getElementById("actionWithAllThreeLevels");

            // Should redirect to login page when DTO role is missing
            button.click();
            assertThat(result.getWindow().location.href).contains("/login.html");
        }

        @Test
        @DisplayName("Action with DTO using alternative DTO role (CONTENT_MANAGER)")
        void testActionWithDtoAlternativeDtoRole() {
            var userInfo = new UserInfoImpl();
            userInfo.setUserId("user1");
            userInfo.setRoles(Set.of("VERIFIED", "SUPPORT", "CONTENT_MANAGER"));

            var testContext = IntegrationTestContext.builder()
                    .withSingleton(HierarchicalRolesTestPage.class)
                    .withLoggedInUser(userInfo, "passwd")
                    .build();

            var result = testContext.openPage("/hierarchical-roles.html");
            var button = result.getDocument().getElementById("actionWithAllThreeLevels");

            // Should not throw (VERIFIED + SUPPORT + CONTENT_MANAGER)
            button.click();
        }

        @Test
        @DisplayName("Action with DTO but no method roles requires controller AND DTO roles")
        void testActionWithDtoNoMethodRoles() {
            var userInfo = new UserInfoImpl();
            userInfo.setUserId("user1");
            userInfo.setRoles(Set.of("USER", "DATA_EDITOR"));

            var testContext = IntegrationTestContext.builder()
                    .withSingleton(HierarchicalRolesTestPage.class)
                    .withLoggedInUser(userInfo, "passwd")
                    .build();

            var result = testContext.openPage("/hierarchical-roles.html");
            var button = result.getDocument().getElementById("actionWithControllerAndDtoRoles");

            // Should not throw (USER + DATA_EDITOR, no method role required)
            button.click();
        }
        
        @Test
        @DisplayName("DTO-only role requirement without controller or method roles")
        void testDtoRoleOnlyWithoutControllerOrMethodRoles() {
            var userInfo = new UserInfoImpl();
            userInfo.setUserId("user1");
            userInfo.setRoles(Set.of("DATA_EDITOR"));

            var testContext = IntegrationTestContext.builder()
                    .withSingleton(NoControllerRolesTestPage.class)
                    .withLoggedInUser(userInfo, "passwd")
                    .build();

            var result = testContext.openPage("/no-controller-roles.html");
            var button = result.getDocument().getElementById("actionWithDtoRolesOnly");

            // Should not throw - only DTO role required
            button.click();
        }
    }

    @Nested
    @DisplayName("Complex Scenarios")
    class ComplexScenariosTest {

        @Test
        @DisplayName("User with all roles can access everything")
        void testUserWithAllRoles() {
            var userInfo = new UserInfoImpl();
            userInfo.setUserId("superuser");
            userInfo.setRoles(Set.of("USER", "VERIFIED", "ADMIN", "MODERATOR", "SUPPORT", "DATA_EDITOR", "CONTENT_MANAGER"));

            var testContext = IntegrationTestContext.builder()
                    .withSingleton(HierarchicalRolesTestPage.class)
                    .withLoggedInUser(userInfo, "passwd")
                    .build();

            var result = testContext.openPage("/hierarchical-roles.html");

            // All actions should succeed
            result.getDocument().getElementById("actionWithControllerRoleOnly").click();
            result.getDocument().getElementById("actionWithControllerAndMethodRoles").click();
            result.getDocument().getElementById("actionWithAllThreeLevels").click();
            result.getDocument().getElementById("actionWithControllerAndDtoRoles").click();
        }

        @Test
        @DisplayName("Minimal role sets for each action")
        void testMinimalRoleSets() {
            // actionWithControllerRoleOnly: USER
            var userInfo1 = new UserInfoImpl();
            userInfo1.setUserId("user1");
            userInfo1.setRoles(Set.of("USER"));

            var testContext1 = IntegrationTestContext.builder()
                    .withSingleton(HierarchicalRolesTestPage.class)
                    .withLoggedInUser(userInfo1, "passwd")
                    .build();

            testContext1.openPage("/hierarchical-roles.html")
                    .getDocument().getElementById("actionWithControllerRoleOnly").click();

            // actionWithControllerAndMethodRoles: VERIFIED + MODERATOR
            var userInfo2 = new UserInfoImpl();
            userInfo2.setUserId("user2");
            userInfo2.setRoles(Set.of("VERIFIED", "MODERATOR"));

            var testContext2 = IntegrationTestContext.builder()
                    .withSingleton(HierarchicalRolesTestPage.class)
                    .withLoggedInUser(userInfo2, "passwd")
                    .build();

            testContext2.openPage("/hierarchical-roles.html")
                    .getDocument().getElementById("actionWithControllerAndMethodRoles").click();

            // actionWithAllThreeLevels: VERIFIED + SUPPORT + CONTENT_MANAGER
            var userInfo3 = new UserInfoImpl();
            userInfo3.setUserId("user3");
            userInfo3.setRoles(Set.of("VERIFIED", "SUPPORT", "CONTENT_MANAGER"));

            var testContext3 = IntegrationTestContext.builder()
                    .withSingleton(HierarchicalRolesTestPage.class)
                    .withLoggedInUser(userInfo3, "passwd")
                    .build();

            testContext3.openPage("/hierarchical-roles.html")
                    .getDocument().getElementById("actionWithAllThreeLevels").click();
        }
    }
}
