package one.xis.server;

public interface RequestFilter {

    void doFilter(Request request, RequestFilters filterChain);

    default Priority getPriority() {
        return Priority.NORMAL;
    }
}
