package one.xis.server;

public interface RequestFilter {

    void doFilter(ClientRequest request, RequestFilters filterChain);

    default Priority getPriority() {
        return Priority.NORMAL;
    }
}
