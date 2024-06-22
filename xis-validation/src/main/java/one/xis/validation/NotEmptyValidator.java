package one.xis.validation;

import java.lang.reflect.AnnotatedElement;
import java.util.Collection;


class NotEmptyValidator implements AnnotationValidator {


    @Override
    public void validate(AnnotatedElement annotatedElement, Object value) throws ValidatorException {
        validate(value);
    }

    @Override
    public int priority() {
        return AnnotationValidator.FRAMEWORK_PRIORITY;
    }

    private void validate(Object value) throws ValidatorException {
        if (value == null) {
            throw new ValidatorException("notEmpty");
        }
        if (value instanceof String && ((String) value).isEmpty()) {
            throw new ValidatorException("notEmpty");
        }
        if (value instanceof Collection && ((Collection<?>) value).isEmpty()) {
            throw new ValidatorException("notEmpty");
        }
        if (value.getClass().isArray()) {
            assert value instanceof Object[];
            if (((Object[]) value).length == 0) {
                throw new ValidatorException("notEmpty");
            }
        }
    }
}
