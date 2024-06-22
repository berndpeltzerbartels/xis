package one.xis.validation;

import java.lang.reflect.Parameter;

public interface Validation {
    void validate(Parameter parameter, Object parameterValue, ValidationErrors errors);
}
