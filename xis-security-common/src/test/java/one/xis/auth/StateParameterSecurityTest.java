package one.xis.auth;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StateParameterSecurityTest {

    @Test
    void rejectsStateParameterWithTamperedSignature() {
        String state = StateParameter.create("/customers.html", "local");
        String[] parts = state.split("\\.");

        assertThatThrownBy(() -> StateParameter.decode(parts[0] + "." + parts[1].substring(1) + "x"))
                .isInstanceOf(InvalidStateParameterException.class)
                .hasMessageContaining("Invalid state parameter signature");
    }
}
