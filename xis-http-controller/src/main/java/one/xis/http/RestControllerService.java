package one.xis.http;

public interface RestControllerService {
    /**
     * Adds a controller to the service, which is not annotated with @RestController.
     * This is intended to allow dynamic registration of controllers
     *
     * @param basePath   the base path for the controller
     * @param controller the controller object
     */
    void addController(String basePath, Object controller);

    /**
     * Adds a controller to the service, which is annotated with @RestController.
     * This is intended to allow dynamic registration of controllers
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
