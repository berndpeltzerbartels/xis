package one.xis.validation;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.deserialize.DeserializationPostProcessor;
import one.xis.deserialize.ReportedError;
import one.xis.deserialize.ReportedErrorContext;
import one.xis.utils.lang.ClassUtils;

import java.util.Collection;
import java.util.List;

@XISComponent
@RequiredArgsConstructor
class Validation implements DeserializationPostProcessor {

    private final List<Validator<?>> validators;

    @Override
    public void postProcess(ReportedErrorContext reportedErrorContext, Object value, Collection<ReportedError> failed) {
        var validateAnnotation = reportedErrorContext.getAnnotationClass().getAnnotation(Validate.class);
        var validatorClass = validateAnnotation.validatorClass();
        var validator = getValidator(validatorClass);
        var typeParameter = ClassUtils.getGenericInterfacesTypeParameter(validatorClass, Validator.class, 0);
        if (!typeParameter.isAssignableFrom(reportedErrorContext.getTarget().getClass())) {
            throw new IllegalArgumentException("Validator " + validatorClass + " in annotataion " + reportedErrorContext.getAnnotationClass()
                    + " is not applicable to " + reportedErrorContext.getTarget());
        }
        try {
            validator.validate(value, reportedErrorContext.getTarget());
        } catch (ValidatorException e) {
            failed.add(new ReportedError(reportedErrorContext, validateAnnotation.messageKey(), validateAnnotation.globalMessageKey()));
        }


    }

    @SuppressWarnings("unchecked")
    private <T> Validator<T> getValidator(Class<? extends Validator<?>> validatorClass) {
        return (Validator<T>) validators.stream()
                .filter(validatorClass::isInstance)
                .findFirst().orElseGet(() -> ClassUtils.newInstance(validatorClass));
    }
}
