package one.xis.jsc;

class DuplicateKeyException extends RuntimeException {
    DuplicateKeyException(JavascriptComponents<?> components, String key) {
        super(components.getClass().getSimpleName() + ": There is more than one object having the key " + key);
    }
}
