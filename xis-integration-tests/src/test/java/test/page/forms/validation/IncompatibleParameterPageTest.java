package test.page.forms.validation;

import one.xis.context.IntegrationTestContext;
import one.xis.test.dom.Document;
import one.xis.test.dom.Element;
import one.xis.test.dom.InputElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for validation. The form contains two integer fields, one is mandatory, the other is not.
 */
@DisplayName("Validation test. The form contains two integer fields, one is mandatory, the other is not.")
class IncompatibleParameterPageTest {

    @Nested
    @DisplayName("Submit an empty form")
    class EmptyFormTest {

        private IntegrationTestContext appContext;

        @BeforeEach
        void initContext() {
            appContext = IntegrationTestContext.builder()
                    .withSingleton(IncompatibleParameterPage.class)
                    .build();
        }


        @Test
        void test() {
            var result = appContext.openPage(IncompatibleParameterPage.class);
            var document = result.getDocument();
            var button = document.getElementByTagName("button");

            button.click();

            assertThat(integerFieldMandatoryMessage(document).getTextContent()).isEqualTo("erforderlich");
            assertThat(integerField(document).getTextContent()).isEmpty();

        }

    }

    @Nested
    @DisplayName("Submit a form with invalid data in both fields")
    class InvalidDataTest {

        private IntegrationTestContext appContext;

        @BeforeEach
        void initContext() {
            appContext = IntegrationTestContext.builder()
                    .withSingleton(IncompatibleParameterPage.class)
                    .build();
        }

        @Test
        void test() {
            var result = appContext.openPage(IncompatibleParameterPage.class);
            var document = result.getDocument();
            integerFieldMandatory(document).setValue("abc");
            integerField(document).setValue("def");
            var button = document.getElementByTagName("button");

            button.click();

            assertThat(integerFieldMandatoryMessage(document).getTextContent()).isEqualTo("Ungültige Eingabe");
            assertThat(integerFieldMessage(document).getTextContent()).isEqualTo("Ungültige Eingabe");

        }

    }

    InputElement integerField(Document document) {
        return (InputElement) document.getElementById("integerField");
    }

    InputElement integerFieldMandatory(Document document) {
        return (InputElement) document.getElementById("integerFieldMandatory");
    }

    Element integerFieldMessage(Document document) {
        return messageElements(document)
                .filter(e -> e.getAttribute("message-for").equals("integerField"))
                .findFirst().orElseThrow();
    }

    Element integerFieldMandatoryMessage(Document document) {
        return messageElements(document)
                .filter(e -> e.getAttribute("message-for").equals("integerFieldMandatory"))
                .findFirst().orElseThrow();
    }

    Stream<Element> messageElements(Document document) {
        return document.getElementsByTagName("xis:message").stream()
                .filter(Element.class::isInstance)
                .map(Element.class::cast);
    }
}
