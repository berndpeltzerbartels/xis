package test.page.forms.validation.annotation;


import one.xis.validation.Validator;
import one.xis.validation.ValidatorException;

import java.lang.reflect.AnnotatedElement;

public class NotNegativeValidator implements Validator<Number> {
    @Override
    public void validate(Number value, AnnotatedElement annotatedElement) throws ValidatorException {
        if (value == null) return;
        if (value.longValue() < 0) {
            throw new ValidatorException();
        }
    }
}