package one.xis;

import java.util.List;
import java.util.function.Predicate;

public class ValidationUtil {

    public static <T> void validate(T value, Predicate<T> validation, String errorMessage, List<String> messages) {
        if (!validation.test(value)) {
            messages.add(errorMessage);
        }
    }
}
