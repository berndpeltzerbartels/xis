package one.xis.validation;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.deserialize.DeserializationPostProcessor;
import one.xis.deserialize.ReportedError;
import one.xis.deserialize.ReportedErrorContext;
import one.xis.utils.lang.ClassUtils;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.List;

@XISComponent
@RequiredArgsConstructor
class ValidationPostProcessor implements DeserializationPostProcessor {

    private final List<Validator<?>> validators;

    @Override
    public void postProcess(ReportedErrorContext reportedErrorContext, Object value, Collection<ReportedError> failed) {
        var validateAnnotation = reportedErrorContext.getAnnotationClass().getAnnotation(Validate.class);
        var validatorClass = validateAnnotation.validatorClass();
        var validator = getValidator(validatorClass);
        var typeParameter = ClassUtils.getGenericInterfacesTypeParameter(validatorClass, Validator.class, 0);
        if (!typeParameter.isAssignableFrom(getTargetType(reportedErrorContext.getTarget()))) {
            throw new IllegalArgumentException("Validator " + validatorClass + " in annotataion " + reportedErrorContext.getAnnotationClass()
                    + " is not applicable to " + reportedErrorContext.getTarget());
        }
        try {
            validator.validate(value, reportedErrorContext.getTarget());
        } catch (ValidatorException e) {
            failed.add(new ReportedError(reportedErrorContext, validateAnnotation.messageKey(), validateAnnotation.globalMessageKey()));
        }
    }

    private Class<?> getTargetType(AnnotatedElement annotatedElement) {
        if (annotatedElement instanceof Class) {
            return (Class<?>) annotatedElement;
        }
        if (annotatedElement instanceof Field field) {
            return field.getType();
        }
        if (annotatedElement instanceof Parameter parameter) {
            return parameter.getType();
        }
        throw new IllegalArgumentException("Unsupported annotated element: " + annotatedElement);
    }

    @SuppressWarnings("unchecked")
    private <T> Validator<T> getValidator(Class<? extends Validator<?>> validatorClass) {
        return (Validator<T>) validators.stream()
                .filter(validatorClass::isInstance)
                .findFirst().orElseGet(() -> ClassUtils.newInstance(validatorClass));
    }
}
