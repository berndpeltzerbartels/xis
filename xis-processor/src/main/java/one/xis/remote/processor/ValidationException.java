package one.xis.remote.processor;

import javax.lang.model.element.Element;

public class ValidationException extends RuntimeException {

    public ValidationException(Element e, String message) {
        super(e + ": " + message);
    }

    public ValidationException(Class<?> c, String message) {
        super(c + ": " + message);
    }
}
