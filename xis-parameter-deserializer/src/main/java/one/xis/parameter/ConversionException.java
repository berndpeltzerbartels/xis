package one.xis.parameter;

import lombok.Getter;

class ConversionException extends RuntimeException {

    @Getter
    private final Object value;

    ConversionException(Object value) {
        this(null, null, value);
    }

    ConversionException(String message, Object value) {
        this(message, null, value);
    }

    ConversionException(String message, Throwable cause, Object value) {
        super(message, cause);
        this.value = value;
    }

    ConversionException(Throwable cause, Object value) {
        this(null, cause, value);
    }
}
