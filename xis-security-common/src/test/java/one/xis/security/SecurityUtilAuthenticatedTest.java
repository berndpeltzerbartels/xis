package one.xis.security;

import one.xis.Authenticated;
import one.xis.Roles;
import one.xis.UserContext;
import one.xis.auth.AuthenticationException;
import one.xis.auth.AuthorizationException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityUtilAuthenticatedTest {

    @Test
    void authenticatedClassAllowsLoggedInUserWithoutNamedRoles() {
        assertThatNoException().isThrownBy(() ->
                SecurityUtil.checkRoles(AuthenticatedController.class, user(true, Set.of())));
    }

    @Test
    void authenticatedClassRejectsAnonymousUser() {
        assertThatThrownBy(() -> SecurityUtil.checkRoles(AuthenticatedController.class, user(false, Set.of())))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void rolesWinWhenClassHasAuthenticatedAndRoles() {
        assertThatNoException().isThrownBy(() ->
                SecurityUtil.checkRoles(AuthenticatedAdminController.class, user(true, Set.of("ADMIN"))));

        assertThatThrownBy(() -> SecurityUtil.checkRoles(AuthenticatedAdminController.class, user(true, Set.of())))
                .isInstanceOf(AuthorizationException.class);

        assertThatThrownBy(() -> SecurityUtil.checkRoles(AuthenticatedAdminController.class, user(false, Set.of("ADMIN"))))
                .isInstanceOf(AuthenticationException.class)
                .isNotInstanceOf(AuthorizationException.class);
    }

    @Test
    void authenticatedMethodAllowsLoggedInUserWithoutNamedRoles() throws NoSuchMethodException {
        Method method = MethodController.class.getDeclaredMethod("authenticatedAction");

        assertThatNoException().isThrownBy(() -> SecurityUtil.checkRoles(method, user(true, Set.of())));
    }

    @Test
    void rolesWinWhenMethodHasAuthenticatedAndRoles() throws NoSuchMethodException {
        Method method = MethodController.class.getDeclaredMethod("authenticatedAdminAction");

        assertThatNoException().isThrownBy(() -> SecurityUtil.checkRoles(method, user(true, Set.of("ADMIN"))));

        assertThatThrownBy(() -> SecurityUtil.checkRoles(method, user(true, Set.of())))
                .isInstanceOf(AuthorizationException.class);

        assertThatThrownBy(() -> SecurityUtil.checkRoles(method, user(false, Set.of("ADMIN"))))
                .isInstanceOf(AuthenticationException.class)
                .isNotInstanceOf(AuthorizationException.class);
    }

    @Test
    void authenticatedDtoRequiresLoggedInUser() throws NoSuchMethodException {
        Method method = DtoController.class.getDeclaredMethod("save", AuthenticatedDto.class);

        assertThatNoException().isThrownBy(() -> SecurityUtil.checkRoles(method, user(true, Set.of())));
        assertThatThrownBy(() -> SecurityUtil.checkRoles(method, user(false, Set.of())))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void authenticatedParameterRequiresLoggedInUser() throws NoSuchMethodException {
        Method method = ParameterController.class.getDeclaredMethod("save", String.class);

        assertThatNoException().isThrownBy(() -> SecurityUtil.checkRoles(method, user(true, Set.of())));
        assertThatThrownBy(() -> SecurityUtil.checkRoles(method, user(false, Set.of())))
                .isInstanceOf(AuthenticationException.class);
    }

    private static UserContext user(boolean authenticated, Set<String> roles) {
        return new UserContext() {
            @Override
            public String getUserId() {
                return authenticated ? "user" : null;
            }

            @Override
            public Locale getLocale() {
                return Locale.ENGLISH;
            }

            @Override
            public ZoneId getZoneId() {
                return ZoneId.of("UTC");
            }

            @Override
            public String getClientId() {
                return "client";
            }

            @Override
            public boolean isAuthenticated() {
                return authenticated;
            }

            @Override
            public Set<String> getRoles() {
                return roles;
            }
        };
    }

    @Authenticated
    private static class AuthenticatedController {
    }

    @Authenticated
    @Roles("ADMIN")
    private static class AuthenticatedAdminController {
    }

    private static class MethodController {
        @Authenticated
        void authenticatedAction() {
        }

        @Authenticated
        @Roles("ADMIN")
        void authenticatedAdminAction() {
        }
    }

    private static class DtoController {
        void save(AuthenticatedDto dto) {
        }
    }

    private static class ParameterController {
        void save(@Authenticated String value) {
        }
    }

    @Authenticated
    private static class AuthenticatedDto {
    }
}
