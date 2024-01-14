package one.xis.server;

public interface RequestFilter {

    void doFilter(ClientRequest request, ServerResponse response, RequestFilterChain filterChain);

    default Priority getPriority() {
        return Priority.NORMAL;
    }
}
