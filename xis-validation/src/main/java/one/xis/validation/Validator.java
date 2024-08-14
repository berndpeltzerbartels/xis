package one.xis.validation;

import java.lang.reflect.AnnotatedElement;

public interface Validator<T> {

    void validate(T value, AnnotatedElement annotatedElement) throws ValidatorException;
}
