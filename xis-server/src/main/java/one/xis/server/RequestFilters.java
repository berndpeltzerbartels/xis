package one.xis.server;

import one.xis.context.XISComponent;
import one.xis.context.XISInit;
import one.xis.context.XISInject;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@XISComponent
public class RequestFilters {

    @XISInject
    private Collection<RequestFilter> filters;
    private List<RequestFilter> sortedFilters;

    @XISInit
    void init() {
        sortedFilters = filters.stream()
                .sorted(Comparator.comparing(RequestFilter::getPriority))
                .collect(Collectors.toList());
    }

    RequestFilterChain apply(ClientRequest request, ValidatorMessages validatorMessages) {
        var filterChain = new RequestFilterChain();
        for (var filter : sortedFilters) {
            filter.doFilter(request, validatorMessages, this);
            if (filterChain.isInterrupt()) {
                return filterChain;
            }
        }
        return filterChain;
    }


}
