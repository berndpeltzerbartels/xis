package one.xis.security;

import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityUtilTest {

    @Test
    void encodeAndDecodeBase64UrlSafe() throws UnsupportedEncodingException {
        var encoded = SecurityUtil.encodeBase64UrlSafe("testString.bla");
        var decoded = new String(SecurityUtil.decodeBase64UrlSafe(encoded), StandardCharsets.UTF_8);

        assertThat(decoded).isEqualTo("testString.bla");
    }
}