package one.xis.validation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public class ValidationFailedException extends RuntimeException {
    private final Map<String, ValidationError> errors;
}
