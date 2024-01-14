package one.xis.server;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;


class RequestFilterChain {
    private final List<RequestFilter> filters;

    @Setter
    @Getter
    private ServerResponse serverResponse;

    RequestFilterChain(BiConsumer<ClientRequest, ServerResponse> responder, Collection<RequestFilter> requestFilters) {
        this.filters = new ArrayList<>(requestFilters);
        filters.sort(Comparator.comparing(RequestFilter::getPriority));
    }

    void doFilter(ClientRequest request, ServerResponse response, RequestFilterChain filterChain) {
        serverResponse = response;
        if (!filters.isEmpty()) {
            var filter = filters.remove(0);
            filter.doFilter(request, serverResponse, filterChain);
        }
    }

}
