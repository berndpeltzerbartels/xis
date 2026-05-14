package one.xis.http;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Getter
@RequiredArgsConstructor
public class RequestContext {
    private final HttpRequest request;
    private final HttpResponse response;

    private final Map<String, Object> attributes = new HashMap<>();
    private final List<RequestContextResource> resources = new ArrayList<>();

    private static final ThreadLocal<RequestContext> context = new ThreadLocal<>();

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrCreateAttribute(String name, Supplier<T> supplier) {
        Object value = attributes.get(name);
        if (value == null) {
            value = supplier.get();
            attributes.put(name, value);
        }
        return (T) value;
    }

    public void registerResource(RequestContextResource resource) {
        resources.add(resource);
    }

    public void closeResources(Throwable failure) {
        List<RequestContextResource> closeOrder = new ArrayList<>(resources);
        Collections.reverse(closeOrder);
        RuntimeException closeFailure = null;
        for (RequestContextResource resource : closeOrder) {
            try {
                resource.close(failure);
            } catch (RuntimeException e) {
                if (closeFailure == null) {
                    closeFailure = e;
                } else {
                    closeFailure.addSuppressed(e);
                }
            }
        }
        resources.clear();
        if (closeFailure != null) {
            throw closeFailure;
        }
    }

    public static RequestContext getInstance() {
        return context.get();
    }

    public static void createInstance(HttpRequest request, HttpResponse response) {
        context.set(new RequestContext(request, response));
    }

    public static void removeInstance() {
        context.remove();
    }

    public static void clear() {
        context.remove();
    }
}
