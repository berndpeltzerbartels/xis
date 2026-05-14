package one.xis.validation;

import lombok.NonNull;
import one.xis.ImportInstances;
import one.xis.UserContext;

import java.lang.reflect.AnnotatedElement;

@ImportInstances
public interface Validator<T> {

    void validate(@NonNull T value, @NonNull AnnotatedElement annotatedElement, @NonNull UserContext userContext) throws ValidatorException;
}
