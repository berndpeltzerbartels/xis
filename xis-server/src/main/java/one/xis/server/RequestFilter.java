package one.xis.server;

public interface RequestFilter {

    void doFilter(ClientRequest request, ValidatorMessages validatorMessages, RequestFilters filterChain);

    default Priority getPriority() {
        return Priority.NORMAL;
    }
}
