package one.xis.validation;

import one.xis.context.XISComponent;

import java.lang.reflect.AnnotatedElement;
import java.util.regex.Pattern;

@XISComponent
class EMailValidator implements Validator<String> {

    private static final Pattern PATTERN_TO_MATCH = Pattern.compile("^[a-zA-Z0-9.-\\.]+@[a-zA-Z0-9-\\.]+[a-z]{2,4}$");
    private static final Pattern PATTERN_NOT_TO_MATCH = Pattern.compile("\\.\\.+|@\\.+|@\\.+\\.|@\\.+\\.+|@\\.+\\.+\\.");

    @Override
    public void validate(String value, AnnotatedElement annotatedElement) throws ValidatorException {
        if (value == null || value.isBlank()) {
            return;
        }
        if (!PATTERN_TO_MATCH.matcher(value).matches() || PATTERN_NOT_TO_MATCH.matcher(value).find()) {
            throw new ValidatorException();
        }
    }
}
