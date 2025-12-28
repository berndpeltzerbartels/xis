package one.xis.theme.example.contact;

import one.xis.UserContext;
import one.xis.context.XISComponent;
import one.xis.validation.Validator;
import one.xis.validation.ValidatorException;

import java.lang.reflect.AnnotatedElement;
import java.util.regex.Pattern;

/**
 * Validator for international phone numbers.
 * Accepts various formats: +1-555-0123, (555) 123-4567, +49 30 12345678, etc.
 */
@XISComponent
public class PhoneNumberValidator implements Validator<String> {

    // Allows: + at start, digits, spaces, dashes, parentheses
    // Minimum 7 digits for valid phone number
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9\\s().-]{7,20}$");

    @Override
    public void validate(String value, AnnotatedElement annotatedElement, UserContext userContext) throws ValidatorException {
        if (value == null || value.isBlank()) {
            return; // null/empty handled by @Mandatory if needed
        }
        
        if (!PHONE_PATTERN.matcher(value).matches()) {
            throw new ValidatorException();
        }
    }
}
