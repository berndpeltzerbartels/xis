package one.xis.remote.js2;

class JSArrayElement implements JSStatementPart {
    private final String value;

    JSArrayElement(String array, String key) {
        value = String.format("%s[%s]", array, key);
    }

    JSArrayElement(JSStatementPart part, String key) {
        this(part.getRef(), key);
    }

    JSArrayElement(String array, JSString key) {
        this(array, key.getContent());
    }

    @Override
    public String getRef() {
        return value;
    }
}
