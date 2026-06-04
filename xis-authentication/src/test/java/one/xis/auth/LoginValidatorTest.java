package one.xis.auth;

import one.xis.UserContext;
import one.xis.context.AppContext;
import one.xis.validation.ValidatorException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginValidatorTest {

    @Test
    void acceptsPasswordWhenNoAdditionalFactorIsRequired() {
        LoginValidator validator = new LoginValidator(appContext(new Credentials(true)), List.of(new Factor(false, false)));

        assertThatCode(() -> validator.validate(new LoginData("mara", "secret", null, "state"), getClass(), userContext()))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsWhenAdditionalFactorIsRequiredAndInvalid() {
        LoginValidator validator = new LoginValidator(appContext(new Credentials(true)), List.of(new Factor(true, false)));

        var login = new LoginData("mara", "secret", "000000", "state");

        assertThatThrownBy(() -> validator.validate(login, getClass(), userContext()))
                .isInstanceOf(ValidatorException.class);
    }

    @Test
    void acceptsWhenAdditionalFactorIsRequiredAndValid() {
        LoginValidator validator = new LoginValidator(appContext(new Credentials(true)), List.of(new Factor(true, true)));

        assertThatCode(() -> validator.validate(new LoginData("mara", "secret", "123456", "state"), getClass(), userContext()))
                .doesNotThrowAnyException();
    }

    private UserContext userContext() {
        return new UserContext() {
            @Override
            public java.util.Locale getLocale() {
                return java.util.Locale.ROOT;
            }

            @Override
            public java.time.ZoneId getZoneId() {
                return java.time.ZoneOffset.UTC;
            }

            @Override
            public String getClientId() {
                return "client";
            }

            @Override
            public String getUserId() {
                return "mara";
            }

            @Override
            public java.util.Set<String> getRoles() {
                return java.util.Set.of();
            }

            @Override
            public boolean isAuthenticated() {
                return false;
            }
        };
    }

    private record Factor(boolean required, boolean valid) implements AdditionalLoginFactor {
        @Override
        public String fieldName() {
            return "totpCode";
        }

        @Override
        public boolean isRequired(String userId) {
            return required;
        }

        @Override
        public boolean verify(String userId, String value) {
            return valid && "123456".equals(value);
        }
    }

    private AppContext appContext(LocalCredentialService credentialService) {
        var appContext = org.mockito.Mockito.mock(AppContext.class);
        org.mockito.Mockito.when(appContext.getOptionalSingleton(LocalCredentialService.class)).thenReturn(Optional.of(credentialService));
        return appContext;
    }

    private record Credentials(boolean credentialsValid) implements LocalCredentialService {
        @Override
        public boolean validateCredentials(String userId, String password) {
            return credentialsValid;
        }

        @Override
        public void setPassword(String userId, String password) {
        }

        @Override
        public boolean needsRehash(String userId) {
            return false;
        }
    }
}
