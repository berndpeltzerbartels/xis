package one.xis.parameter;

import one.xis.UserContext;
import one.xis.server.ValidationError;

import java.io.IOException;
import java.lang.reflect.Parameter;
import java.util.Map;

public interface ParameterDeserializer {
    Object deserialize(String paramValue, Parameter parameter, Map<String, ValidationError> errors, UserContext userContex) throws IOException;

}
