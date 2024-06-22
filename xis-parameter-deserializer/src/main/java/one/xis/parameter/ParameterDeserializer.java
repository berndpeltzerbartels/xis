package one.xis.parameter;

import one.xis.UserContext;
import one.xis.validation.ValidationErrors;

import java.io.IOException;
import java.lang.reflect.Parameter;

public interface ParameterDeserializer {
    Object deserialize(String paramValue, Parameter parameter, ValidationErrors errors, UserContext userContex) throws IOException;

}
