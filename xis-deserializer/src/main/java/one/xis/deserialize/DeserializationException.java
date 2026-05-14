package one.xis.deserialize;

import lombok.Getter;

class DeserializationException extends RuntimeException {

    @Getter
    private final Object userInput;

    DeserializationException(Throwable cause, Object userInput) {
        super(cause);
        this.userInput = userInput;
    }

    DeserializationException(String message, Object userInput) {
        super(message);
        this.userInput = userInput;
    }

}
