package one.xis.totp;

import one.xis.context.AppContext;
import one.xis.context.AppContextInitializedEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TOTPStartupValidatorTest {

    @AfterEach
    void clearProperties() {
        System.clearProperty("xis.totp.encryption-key");
    }

    @Test
    void failsWithoutStore() {
        System.setProperty("xis.totp.encryption-key", "test-key");

        assertThatThrownBy(() -> validate(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("TOTPStore");
    }

    @Test
    void failsWithoutEncryptionKey() {
        assertThatThrownBy(() -> validate(List.of(new Store())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("xis.totp.encryption-key");
    }

    @Test
    void acceptsSingleStoreAndEncryptionKey() {
        System.setProperty("xis.totp.encryption-key", "test-key");

        assertThatCode(() -> validate(List.of(new Store()))).doesNotThrowAnyException();
    }

    private void validate(Collection<Object> singletons) {
        new TOTPStartupValidator(new TOTPConfig()).validate(new AppContextInitializedEvent(context(singletons)));
    }

    private AppContext context(Collection<Object> singletons) {
        return new AppContext() {
            @Override
            public <T> T getSingleton(Class<T> type) {
                return type.cast(singletons.stream().filter(type::isInstance).findFirst().orElseThrow());
            }

            @Override
            public <T> Optional<T> getOptionalSingleton(Class<T> type) {
                return singletons.stream().filter(type::isInstance).findFirst().map(type::cast);
            }

            @Override
            public Collection<Object> getSingletons() {
                return singletons;
            }
        };
    }

    private static class Store implements TOTPStore {
        @Override
        public Optional<String> getEncryptedSecret(String userId) {
            return Optional.empty();
        }

        @Override
        public void saveEncryptedSecret(String userId, String encryptedSecret) {
        }
    }
}
