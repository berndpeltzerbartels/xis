package one.xis.validation;

import one.xis.ImportInstances;

import java.lang.reflect.AnnotatedElement;

@ImportInstances
public interface Validator<T> {

    void validate(T value, AnnotatedElement annotatedElement) throws ValidatorException;
}
