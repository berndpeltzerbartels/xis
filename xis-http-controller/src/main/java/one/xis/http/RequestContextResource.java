package one.xis.http;

/**
 * Resource bound to the current {@link RequestContext}.
 *
 * <p>Implementations are closed when the request context ends. The optional
 * {@code failure} argument is the exception that escaped request processing, or
 * {@code null} when the request completed normally.</p>
 */
public interface RequestContextResource {
    void close(Throwable failure);
}
