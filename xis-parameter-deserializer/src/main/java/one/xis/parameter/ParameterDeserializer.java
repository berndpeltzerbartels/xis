package one.xis.parameter;

import one.xis.validation.ValidatorResultElement;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.time.ZoneId;
import java.util.Locale;

public interface ParameterDeserializer {
    Object deserialze(String json, Parameter parameter, ValidatorResultElement parameterResult, Locale locale, ZoneId zoneId) throws IOException;

    Object deserialze(String json, Field field, ValidatorResultElement parameterResult, Locale locale, ZoneId zoneId) throws IOException;

    Object deserializeParameter(String paramValue, Parameter parameter, ValidatorResultElement validatorResultElement, Locale locale, String zoneId) throws IOException;
}
