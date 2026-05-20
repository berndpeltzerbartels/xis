package one.xis.totp;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QRCodeSvgRendererTest {

    @Test
    void rendersSvgQrCode() {
        String svg = new QRCodeSvgRenderer().render("otpauth://totp/XIS:mara?secret=ABC&issuer=XIS");

        assertThat(svg).startsWith("<svg");
        assertThat(svg).contains("<path fill=\"#000\"");
        assertThat(svg).endsWith("</svg>");
    }
}
