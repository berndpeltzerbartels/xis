package one.xis.validation;

import one.xis.context.XISComponent;
import one.xis.context.XISInject;

import java.lang.reflect.Parameter;
import java.util.Collection;

@XISComponent
public class ValidationImpl implements Validation {

    @XISInject
    @SuppressWarnings("rawtypes")
    private Collection<TypeValidator<?>> validators;

    @XISInject
    private AnnotationValidation annotationValidation;


    @Override
    public void validate(Parameter parameter, Object parameterValue, ValidationErrors errors) {
        annotationValidation.validate(parameter, parameterValue, errors);
        new TypeValidation(validators, errors).validate(parameter, parameterValue);
    }
}
