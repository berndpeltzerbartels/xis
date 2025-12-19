package one.xis.validation;

import one.xis.ImportInstances;
import one.xis.UserContext;

import java.lang.reflect.AnnotatedElement;

@ImportInstances
public interface Validator<T> {

    void validate(T value, AnnotatedElement annotatedElement, UserContext userContext) throws ValidatorException;
}
