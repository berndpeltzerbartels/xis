package one.xis.auth;

import lombok.Getter;

public class InvalidStateParameterException extends AuthorizationException {

    @Getter
    private StateParameterPayload stateParameterPayload;

    public InvalidStateParameterException() {
    }

    public InvalidStateParameterException(String message) {
        super(message);
    }

    public InvalidStateParameterException(String message, StateParameterPayload stateParameterPayload) {
        super(message);
        this.stateParameterPayload = stateParameterPayload;
    }

    public InvalidStateParameterException(String message, Exception e) {
        super(message, e);
    }
}
