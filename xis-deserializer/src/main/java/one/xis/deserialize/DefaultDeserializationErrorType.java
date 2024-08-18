package one.xis.deserialize;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
enum DefaultDeserializationErrorType {
    CONVERSION_ERROR("validation.invalid", "validation.invalid.global"),
    MISSING_MANDATORY_PROPERTY("validation.mandatory", "validation.mandatory.global");
    private final String messageKey;
    private final String globalMessageKey;
}
