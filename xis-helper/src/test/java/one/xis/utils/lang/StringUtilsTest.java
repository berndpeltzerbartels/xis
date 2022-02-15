package one.xis.utils.lang;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StringUtilsTest {

    @Test
    void isSeparatorsOnly() {
        assertThat(StringUtils.isSeparatorsOnly(" \n")).isTrue();
        assertThat(StringUtils.isSeparatorsOnly(" \nx")).isFalse();
        assertThat(StringUtils.isSeparatorsOnly("x \nx")).isFalse();
    }
}