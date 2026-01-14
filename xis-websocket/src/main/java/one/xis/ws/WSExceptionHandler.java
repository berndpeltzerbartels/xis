package one.xis.ws;

public interface WSExceptionHandler<T extends Throwable> {

    WSServerResponse handleException(T exception);

}
