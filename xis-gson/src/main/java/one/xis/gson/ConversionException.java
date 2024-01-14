package one.xis.gson;

import lombok.Getter;

public class ConversionException extends RuntimeException {

    @Getter
    private final Object value;

    public ConversionException(Object value) {
        this(null, null, value);
    }

    public ConversionException(String message, Object value) {
        this(message, null, value);
    }

    public ConversionException(String message, Throwable cause, Object value) {
        super(message, cause);
        this.value = value;
    }

    public ConversionException(Throwable cause, Object value) {
        this(null, cause, value);
    }
}
