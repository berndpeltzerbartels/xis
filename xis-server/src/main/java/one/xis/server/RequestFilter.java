package one.xis.server;

import one.xis.validation.ValidatorMessages;

public interface RequestFilter {

    void doFilter(ClientRequest request, ValidatorMessages validatorMessages, RequestFilters filterChain);

    default Priority getPriority() {
        return Priority.NORMAL;
    }
}
