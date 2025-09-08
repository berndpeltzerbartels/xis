package one.xis.html.tokens;

public class HtmlParseException extends RuntimeException {
    public HtmlParseException() {
    }

    public HtmlParseException(Throwable cause) {
        super(cause);
    }

    public HtmlParseException(String message) {
        super(message);
    }

    public HtmlParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public HtmlParseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
