package one.xis.validation;

import lombok.NonNull;
import one.xis.UserContext;
import one.xis.context.Component;

import java.lang.reflect.AnnotatedElement;

@Component
class RegExprValidator implements Validator<String> {
    @Override
    public void validate(@NonNull String value, @NonNull AnnotatedElement annotatedElement, @NonNull UserContext userContext) throws ValidatorException {
        if (!value.matches(annotatedElement.getAnnotation(RegExpr.class).value())) {
            throw new ValidatorException();
        }
    }
}
