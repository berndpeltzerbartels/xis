package one.xis.deserialize;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
enum DefaultDeserializationErrorType {
    CONVERSION_ERROR("validation.invalid", "validation.invalidGlobal"),
    MISSING_MANDATORY_PROPERTY("validation.notEmpty", "validation.notEmptyGlobal");
    private final String messageKey;
    private final String globalMessageKey;
}
