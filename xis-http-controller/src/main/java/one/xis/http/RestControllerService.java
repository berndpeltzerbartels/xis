package one.xis.http;

public interface RestControllerService {
    /**
     * Dynamically registers a plain HTTP controller instance under the given base path.
     *
     * @param basePath   the base path for the controller
     * @param controller the controller object
     */
    void addController(String basePath, Object controller);

    /**
     * Dynamically registers an exception handler for plain HTTP controller invocations.
     *
     * @param handler the controller exception handler
     */
    void addExceptionHandler(ControllerExceptionHandler<?> handler);

    /**
     * Invokes the controller method for the given HTTP request.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     */
    void doInvocation(HttpRequest request, HttpResponse response);
}
