package one.xis.validation;

import lombok.NonNull;
import one.xis.FormData;
import one.xis.UserContext;
import one.xis.context.AppContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AnnotationValidationTest {

    private AnnotationValidation annotationValidation;
    private Parameter parameter;

    @Target({ElementType.FIELD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @ValidatorClass(TestAnnotationValidator.class)
    @interface TestAnnotation {
    }

    static class TestAnnotationValidator implements AnnotationValidator {

        @Override
        public void validate(@NonNull AnnotatedElement annotatedElement, @NonNull Object value) throws ValidatorException {
            if (value instanceof Number) {
                if (((Number) value).intValue() < 0) {
                    throw new ValidatorException("negative");
                }
            } else {
                throw new IllegalStateException("@TestAnnotation can only be used on numbers");
            }
        }

        @Override
        public String createMessage(String messageKey, Map<String, Object> parameters, Field field, UserContext userContext) {
            return "Number must not be negative";
        }

        @Override
        public String createGlobalMessage(String messageKey, Map<String, Object> parameters, Parameter parameter, UserContext userContext) {
            return "One of the numbers is negative";
        }
    }

    static class TestClass {
        @TestAnnotation
        private int testField;
    }

    void testMethod(@FormData("test") @TestAnnotation int test) {

    }

    @BeforeEach
    void setUp() {
        var context = AppContext.builder()
                .withSingletonClass(TestAnnotationValidator.class)
                .build();
        annotationValidation = new AnnotationValidation(context);
        parameter = Arrays.stream(getClass().getDeclaredMethods()).filter(m -> m.getName().equals("testMethod")).findFirst().orElseThrow().getParameters()[0];
        UserContext.getInstance().setLocale(Locale.US);
    }

    @Test
    void validationOk() {
        var errors = new ValidationErrors();
        annotationValidation.validate(parameter, 0, errors);

        assertThat(errors.getErrors()).isEmpty();
        assertThat(errors.getGlobalErrors()).isEmpty();
    }

    @Test
    void validationFailed() {
        var errors = new ValidationErrors();
        annotationValidation.validate(parameter, -1, errors);

        assertThat(errors.getErrors()).hasSize(1);
        assertThat(errors.getErrors().get("/test")).isEqualTo("Number must not be negative");
        assertThat(errors.getGlobalErrors()).containsExactly("One of the numbers is negative");
    }
}