package one.xis.http;

import lombok.NonNull;

/**
 * Intercepts plain HTTP requests before they reach a controller method.
 *
 * <p>Filters are XIS components. Implementations may inspect or modify the
 * request and response, then call {@link FilterChain#doFilter(HttpRequest,
 * HttpResponse)} to continue processing.</p>
 */
public interface HttpFilter extends Comparable<HttpFilter> {

    /**
     * Handles one request.
     */
    void doFilter(@NonNull HttpRequest request, @NonNull HttpResponse response, @NonNull FilterChain chain);

    /**
     * Filters with higher priority are executed first.
     * Default priority is 100.
     * <p>
     * Low value means high priority.
     *
     * @return the priority of the filter
     */
    default int getPriority() {
        return 100;
    }

    @Override
    default int compareTo(HttpFilter other) {
        return Integer.compare(this.getPriority(), other.getPriority());
    }
}
