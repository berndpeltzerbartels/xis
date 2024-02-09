package one.xis.parameter;

import one.xis.UserContext;

import java.io.IOException;
import java.lang.reflect.Parameter;
import java.util.Map;

public interface ParameterDeserializer {
    Object deserialize(String paramValue, Parameter parameter, Map<String, Throwable> errors, UserContext userContex) throws IOException;

}
