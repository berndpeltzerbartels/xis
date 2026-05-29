package one.xis.http;

/**
 * Continuation passed to {@link HttpFilter} implementations.
 */
public interface FilterChain {
    /**
     * Continues request processing with the next filter or controller.
     */
    void doFilter(HttpRequest request, HttpResponse response);
}
