package one.xis.validation;

import one.xis.UserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationUtilTest {

    @SuppressWarnings("unused")
    private String testField;

    @BeforeEach
    void init() {
        UserContext.getInstance().setLocale(Locale.GERMAN);
    }

    @Test
    void createMessage() {
        var message = ValidationUtil.createMessage("notEmpty", Collections.emptyMap(), ValidationUtilTest.class.getDeclaredFields()[0], UserContext.getInstance());

        assertThat(message).isEqualTo("erforderlich");
    }


    @Test
    void createGlobalMessage() {
        var message = ValidationUtil.createMessage("testMessageKey", Map.of("var1", "${field1}"), ValidationUtilTest.class.getDeclaredFields()[0], UserContext.getInstance());

        assertThat(message).isEqualTo("Das Feld \"blabla\" ist ein Pflichtfeld");
    }
}