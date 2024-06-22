package one.xis.validation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ValidationFailedException extends RuntimeException {
    private final ValidationErrors errors;
}
