package one.xis.validation;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.deserialize.DeserializationContext;
import one.xis.deserialize.DeserializationPostProcessor;
import one.xis.deserialize.InvalidValueError;
import one.xis.deserialize.PostProcessingResults;
import one.xis.utils.lang.ClassUtils;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.List;

@XISComponent
@RequiredArgsConstructor
class ValidationPostProcessor implements DeserializationPostProcessor {

    private final List<Validator<?>> validators;

    @Override
    public void postProcess(DeserializationContext deserializationContext, Object value, PostProcessingResults postProcessingResults) {
        var validateAnnotation = deserializationContext.getAnnotationClass().getAnnotation(Validate.class);
        var validatorClass = validateAnnotation.validatorClass();
        var validator = getValidator(validatorClass);
        var typeParameter = ClassUtils.getGenericInterfacesTypeParameter(validatorClass, Validator.class, 0);
        if (!typeParameter.isAssignableFrom(getTargetType(deserializationContext.getTarget()))) {
            throw new IllegalArgumentException("Validator " + validatorClass + " in annotation " + deserializationContext.getAnnotationClass()
                    + " is not applicable to " + deserializationContext.getTarget());
        }
        try {
            validator.validate(value, deserializationContext.getTarget());
        } catch (ValidatorException e) {
            postProcessingResults.add(new InvalidValueError(deserializationContext, validateAnnotation.messageKey(), validateAnnotation.globalMessageKey(), value, e.getMessageParameters()));
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
