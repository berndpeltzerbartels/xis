package one.xis.validation;

import lombok.NonNull;
import one.xis.UserContext;
import one.xis.context.Component;

import java.lang.reflect.AnnotatedElement;
import java.util.regex.Pattern;

@Component
class EMailValidator implements Validator<String> {

    private static final Pattern PATTERN_TO_MATCH = Pattern.compile("^[a-zA-Z0-9.-\\.]+@[a-zA-Z0-9-\\.]+[a-z]{2,4}$");
    private static final Pattern PATTERN_NOT_TO_MATCH = Pattern.compile("\\.\\.+|@\\.+|@\\.+\\.|@\\.+\\.+|@\\.+\\.+\\.");

    @Override
    public void validate(@NonNull String value, @NonNull AnnotatedElement annotatedElement, @NonNull UserContext userContext) throws ValidatorException {
        if (!PATTERN_TO_MATCH.matcher(value).matches() || PATTERN_NOT_TO_MATCH.matcher(value).find()) {
            throw new ValidatorException();
        }
    }
}
