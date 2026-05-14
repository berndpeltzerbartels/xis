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

    @Test
    void removeLastChar() {
        assertThat(StringUtils.removeLastChar("1")).isEqualTo("");
        assertThat(StringUtils.removeLastChar("abc")).isEqualTo("ab");
    }

    @Test
    void removeFirstChar() {
        assertThat(StringUtils.removeFirstChar("1")).isEqualTo("");
        assertThat(StringUtils.removeFirstChar("abc")).isEqualTo("bc");
    }

    @Test
    void before() {
        assertThat(StringUtils.before("abc", 'b')).isEqualTo("a");
        assertThat(StringUtils.before("abc", 'c')).isEqualTo("ab");
        assertThat(StringUtils.before("abc", 'a')).isEqualTo("");
        assertThat(StringUtils.before("abc", 'd')).isEqualTo("abc");
        assertThat(StringUtils.before("", 'a')).isEqualTo("");
    }
}