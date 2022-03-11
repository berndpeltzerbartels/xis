package one.xis.template;

import org.w3c.dom.Element;

public class TemplateSynthaxException extends RuntimeException {
    // TODO create thread-local context to mention file-name
    public TemplateSynthaxException(String message) {
        super(message);
    }

    public TemplateSynthaxException(Element e, String message) {
        super(message);
    }
}
