package one.xis.server;

import one.xis.ImportInstances;

@ImportInstances
public interface RequestFilter {

    void doFilter(ClientRequest request, ServerResponse response, RequestFilterChain filterChain);

    default Priority getPriority() {
        return Priority.NORMAL;
    }
}
