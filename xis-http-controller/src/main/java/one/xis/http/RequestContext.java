package one.xis.http;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class RequestContext {
    private final HttpRequest request;
    private final HttpResponse response;

    private final Map<String, Object> attributes = new HashMap<>();

    private static final ThreadLocal<RequestContext> context = new ThreadLocal<>();

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
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
