package one.xis.validation;

import lombok.AllArgsConstructor;
import lombok.Data;
import one.xis.FormData;
import one.xis.UserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TypeValidationTest {

    private TypeValidation typeValidation;
    private ValidationErrors errors;
    private Parameter parameter;

    @Data
    @AllArgsConstructor
    static class TestObject1 {
        private String field1;
        private String field2;
        private TestObject2 testObject2;
    }

    @Data
    @AllArgsConstructor
    static class TestObject2 {
        private String field3;
        private String field4;
    }


    class TestObject1Validator implements TypeValidator<TestObject1> {
        @Override
        public void validate(TestObject1 value, Errors errors) {
            if (value.field1 == null) {
                errors.addFieldError("field1", "notEmpty");
                errors.addGlobalError("notEmptyGlobal");
            }
            if (value.field2 == null) {
                errors.addFieldError("field2", "notEmpty");
                errors.addGlobalError("notEmptyGlobal");
            }
        }
    }

    class TestObject2Validator implements TypeValidator<TestObject2> {
        @Override
        public void validate(TestObject2 value, Errors errors) {
            if (value.field3 == null) {
                errors.addFieldError("field3", "notEmpty");
                errors.addGlobalError("notEmptyGlobal");
            }
            if (value.field4 == null) {
                errors.addFieldError("field4", "notEmpty");
                errors.addGlobalError("notEmptyGlobal");
            }
        }
    }

    class TestBean {
        @SuppressWarnings("unused")
        void method(@FormData("testObject") TestObject1 testObject1) {
        }
    }

    @BeforeEach
    void setUp() {
        errors = new ValidationErrors();
        typeValidation = new TypeValidation(Set.of(new TestObject1Validator(), new TestObject2Validator()), errors);
        parameter = Arrays.stream(TestBean.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("method")).findFirst().orElseThrow().getParameters()[0];
        UserContext.getInstance().setLocale(Locale.GERMAN);
    }

    @Test
    void validationOk() {
        var testObject = new TestObject1("value", "value", new TestObject2("value", "value"));
        typeValidation.validate(parameter, testObject);

        assertThat(errors.isEmpty()).isTrue();
    }

    @Test
    void validationOkWhenNesteObjectIsNull() {
        var testObject = new TestObject1("value", "value", null);
        typeValidation.validate(parameter, testObject);

        assertThat(errors.isEmpty()).isTrue();
    }

    @Test
    void validationFailed() {
        var testObject = new TestObject1(null, "value", null);
        typeValidation.validate(parameter, testObject);

        assertThat(errors.getErrorPaths()).hasSize(1).containsExactly("/testObject/field1");
        assertThat(errors.getError("/testObject/field1")).isEqualTo("erforderlich");

        assertThat(errors.getGlobalErrors()).containsExactly("Nicht alle erforderlichen Felder wurden ausgefüllt");
    }

    @Test
    void validateWithNestedObject() {
        var testObject = new TestObject1("value", "value", new TestObject2(null, "value"));
        typeValidation.validate(parameter, testObject);

        assertThat(errors.getErrors()).hasSize(1).containsKey("/testObject/testObject2/field3");
    }
}