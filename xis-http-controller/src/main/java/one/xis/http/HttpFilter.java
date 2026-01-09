package one.xis.http;

import lombok.NonNull;

public interface HttpFilter extends Comparable<HttpFilter> {

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
