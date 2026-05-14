package one.xis.context;

class SingletonNotFoundException extends RuntimeException {
    public <T> SingletonNotFoundException(Class<T> type) {
        super("Singleton of type " + type.getName() + " not found");
    }
}
