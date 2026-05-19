package one.xis.theme;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XisThemeMessageHandlerSecurityTest {

    @Test
    void toastMessagesAreRenderedAsText() throws Exception {
        try (var input = getClass().getClassLoader().getResourceAsStream("js/message-handler.js")) {
            assertNotNull(input);
            String script = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertTrue(script.contains("toastDiv.textContent"));
            assertFalse(script.contains("toastDiv.innerHTML = message"));
        }
    }
}
