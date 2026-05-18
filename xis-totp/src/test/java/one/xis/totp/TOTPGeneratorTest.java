package one.xis.totp;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class TOTPGeneratorTest {

    @Test
    void createsRfcCompatibleCode() {
        TOTPGenerator generator = new TOTPGenerator();

        assertThat(generator.code("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ", 1)).isEqualTo("287082");
    }

    @Test
    void verifiesCurrentCode() {
        TOTPGenerator generator = new TOTPGenerator();
        generator.setClock(Clock.fixed(Instant.ofEpochSecond(59), ZoneOffset.UTC));

        assertThat(generator.verify("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ", "287082")).isTrue();
        assertThat(generator.verify("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ", "000000")).isFalse();
    }

    @Test
    void verifiesPreviousCode() {
        TOTPGenerator generator = new TOTPGenerator();
        generator.setClock(Clock.fixed(Instant.ofEpochSecond(61), ZoneOffset.UTC));

        assertThat(generator.verify("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ", "287082")).isTrue();
    }
}
