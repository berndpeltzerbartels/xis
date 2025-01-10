package one.xis.context2;

class SingletonNotFoundException extends RuntimeException {
    public <T> SingletonNotFoundException(Class<T> type) {
        super("Singleton of type " + type.getName() + " not found");
    }
}
