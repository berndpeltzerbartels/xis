package one.xis.totp;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class Base32Test {

    @Test
    void encodesAndDecodesWithoutPadding() {
        String encoded = Base32.encode("hello".getBytes(StandardCharsets.UTF_8));

        assertThat(encoded).isEqualTo("NBSWY3DP");
        assertThat(new String(Base32.decode(encoded), StandardCharsets.UTF_8)).isEqualTo("hello");
    }
}
