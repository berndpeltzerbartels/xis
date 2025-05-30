package one.xis.test.dom;

class DomAssertionException extends LoggedRuntimeException {

    DomAssertionException(Element e, String message, Object... args) {
        super("<" + e.localName + ">: " + String.format(message, args));
    }

    DomAssertionException(String message, Object... args) {
        super(String.format(message, args));
    }
}
