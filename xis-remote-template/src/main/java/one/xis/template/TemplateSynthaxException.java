package one.xis.template;

import org.w3c.dom.Element;

public class TemplateSynthaxException extends RuntimeException {
    public TemplateSynthaxException(String message) {
        super(message);
    }

    public TemplateSynthaxException(Element e, String message) {
        super(message);
    }
}
