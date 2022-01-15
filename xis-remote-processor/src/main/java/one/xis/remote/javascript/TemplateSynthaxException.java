package one.xis.remote.javascript;

import org.w3c.dom.Element;

public class TemplateSynthaxException extends Exception {
    public TemplateSynthaxException(String message) {
        super(message);
    }

    public TemplateSynthaxException(Element e, String message) {
        super(message);
    }
}
