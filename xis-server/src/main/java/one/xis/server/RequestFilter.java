package one.xis.server;

public interface RequestFilter {

    void doFilter(ClientRequest request, ValidationResult validationResult, RequestFilters filterChain);

    default Priority getPriority() {
        return Priority.NORMAL;
    }
}
