package one.xis.ws;

public interface WSExceptionHandler<T extends Throwable> {

    WSServerResponse handleException(WSClientRequest request, T exception);

}
