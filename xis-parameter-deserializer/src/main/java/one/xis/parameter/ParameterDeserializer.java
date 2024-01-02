package one.xis.parameter;

import one.xis.validation.ValidatorResultElement;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Optional;

public interface ParameterDeserializer {
    Optional<Object> deserialize(String json, Field field, ValidatorResultElement parameterResult, Locale locale, ZoneId zoneId) throws IOException;

    Optional<Object> deserialize(String paramValue, Parameter parameter, ValidatorResultElement validatorResultElement, Locale locale, ZoneId zoneId) throws IOException;
}
