package one.xis.http;

import lombok.NonNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.BiConsumer;


class FilterChainImpl implements FilterChain {

    private final Deque<HttpFilter> filters;
    private final BiConsumer<HttpRequest, HttpResponse> controllerInvocation;

    public FilterChainImpl(@NonNull List<HttpFilter> filters,
                           @NonNull BiConsumer<HttpRequest, HttpResponse> controllerInvocation) {
        this.filters = new ArrayDeque<>(filters);
        this.controllerInvocation = controllerInvocation;

    }

    @Override
    public void doFilter(HttpRequest request, HttpResponse response) {
        HttpFilter filter = filters.pollFirst();
        if (filter != null) {
            filter.doFilter(request, response, this);
        } else {
            controllerInvocation.accept(request, response);
        }

    }
}
